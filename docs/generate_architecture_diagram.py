#!/usr/bin/env python3
"""
TradingPT Dev Server - AWS Architecture Diagram Generator
Includes CI/CD pipeline (GitHub Actions, ECR, CodeDeploy) and infrastructure
"""

from diagrams import Diagram, Cluster, Edge
from diagrams.aws.compute import EC2, ECR, ECS
from diagrams.aws.network import ELB, VPC, InternetGateway, NATGateway, RouteTable
from diagrams.aws.database import RDS
from diagrams.aws.security import SecretsManager
from diagrams.aws.storage import S3
from diagrams.aws.devtools import Codedeploy
from diagrams.aws.management import SystemsManager, Cloudwatch
from diagrams.onprem.vcs import Github
from diagrams.onprem.ci import GithubActions
from diagrams.custom import Custom

# Diagram attributes for better visualization
graph_attr = {
    "fontsize": "14",
    "bgcolor": "white",
    "pad": "0.5",
    "splines": "ortho",
    "nodesep": "0.8",
    "ranksep": "1.2",
}

with Diagram(
    "TradingPT Dev Server - AWS Architecture with CI/CD",
    filename="tpt_dev_architecture",
    direction="TB",
    graph_attr=graph_attr,
    show=False,
):

    # ========================================
    # CI/CD Pipeline (Top Section)
    # ========================================
    with Cluster("CI/CD Pipeline", graph_attr={"bgcolor": "#E8F5E9"}):
        github = Github("GitHub\nRepository\n(develop branch)")

        with Cluster("GitHub Actions Workflow"):
            actions = GithubActions("GitHub Actions\n(Build & Deploy)")

        with Cluster("AWS Services"):
            ecr = ECR("ECR\ntpt-server-dev\n(Docker Registry)")
            s3_deploy = S3("S3\ntpt-dev-deployments\n(Deployment Package)")
            codedeploy_app = Codedeploy("CodeDeploy\ntpt-server-dev\n(Deployment Group: Develop)")
            ssm = SystemsManager("Parameter Store\n/tpt-api/dev/\n(Environment Variables)")
            cloudwatch = Cloudwatch("CloudWatch Logs\n/tpt/dev/application")

        # CI/CD Flow
        github >> Edge(label="push/merge", style="bold", color="darkgreen") >> actions
        actions >> Edge(label="1. Build & Push Docker Image", color="blue") >> ecr
        actions >> Edge(label="2. Upload Deploy Package", color="blue") >> s3_deploy
        actions >> Edge(label="3. Trigger Deployment", color="blue") >> codedeploy_app

    # ========================================
    # AWS VPC Infrastructure
    # ========================================
    with Cluster("VPC: tpt-dev-vpc (10.0.0.0/16)\nRegion: ap-northeast-2", graph_attr={"bgcolor": "#E3F2FD"}):

        igw = InternetGateway("Internet Gateway\nigw-0114bc04a2ef5d9cf")

        # Public Subnets
        with Cluster("Availability Zone 2a", graph_attr={"bgcolor": "#FFF3E0"}):
            with Cluster("Public Subnet A\n10.0.1.0/24", graph_attr={"bgcolor": "#C8E6C9"}):
                nat_gateway = NATGateway("NAT Gateway\nnat-0a155b614db973575\nEIP: 13.209.232.146")
                bastion = EC2("Bastion Host\ni-0ad4bd4fb2fa30937\nt2.micro\n10.0.1.31\nPublic: 3.39.6.130")
                public_rt_a = RouteTable("Public RT\nrtb-0ce286c2d8c199790")

            with Cluster("App Private Subnet A\n10.0.3.0/24", graph_attr={"bgcolor": "#FFECB3"}):
                app_server = EC2("App Server\ni-0ff8c7f966ae3dbd8\nt2.micro\n10.0.3.118\n(Docker Container)")
                private_rt = RouteTable("Private RT\nrtb-03dd031908108af95")

            with Cluster("DB Private Subnet A\n10.0.5.0/24", graph_attr={"bgcolor": "#FFCCBC"}):
                db_subnet_a = EC2("DB Subnet A\nsubnet-0250039ac381a6001")

        # Public Subnets AZ C
        with Cluster("Availability Zone 2c", graph_attr={"bgcolor": "#FFF3E0"}):
            with Cluster("Public Subnet C\n10.0.2.0/24", graph_attr={"bgcolor": "#C8E6C9"}):
                public_subnet_c = EC2("Public Subnet C\nsubnet-09bf0aa5940395a1f")

            with Cluster("App Private Subnet C\n10.0.4.0/24", graph_attr={"bgcolor": "#FFECB3"}):
                app_subnet_c = EC2("App Subnet C\nsubnet-05c4b84143a3537d4")

            with Cluster("DB Private Subnet C\n10.0.6.0/24", graph_attr={"bgcolor": "#FFCCBC"}):
                rds = RDS("RDS MySQL 8.0.42\ntpt-dev-db\ndb.t4g.micro\n*.c5iossg2kg21.rds.amazonaws.com:3306")

        # Load Balancer
        alb = ELB("Application Load Balancer\ntpt-dev-alb\nInternet-facing\nDNS: tpt-dev-alb-*.elb.amazonaws.com")
        target_group = ECS("Target Group\ntpt-dev-server-tg\nHTTP:8080\nTarget: i-0ff8c7f966ae3dbd8\nStatus: healthy")

    # ========================================
    # External Users
    # ========================================
    users = Custom("Users/Clients", "./user_icon.png") if False else EC2("Users/Clients")

    # ========================================
    # Network Flow - Internet to Application
    # ========================================
    users >> Edge(label="HTTPS/HTTP", color="red", style="bold") >> igw
    igw >> Edge(label="to ALB", color="red") >> alb
    alb >> Edge(label="HTTP:8080", color="orange") >> target_group
    target_group >> Edge(label="forward", color="orange") >> app_server

    # ========================================
    # Network Flow - SSH Access
    # ========================================
    users >> Edge(label="SSH:22", color="purple", style="dashed") >> bastion
    bastion >> Edge(label="SSH", color="purple", style="dashed") >> app_server

    # ========================================
    # Network Flow - Database Connection
    # ========================================
    app_server >> Edge(label="MySQL:3306", color="brown") >> rds

    # ========================================
    # Network Flow - Outbound Internet (NAT)
    # ========================================
    app_server >> Edge(label="Outbound", color="green", style="dotted") >> nat_gateway
    nat_gateway >> Edge(label="to Internet", color="green", style="dotted") >> igw

    # ========================================
    # Routing
    # ========================================
    public_rt_a >> Edge(label="0.0.0.0/0", style="dashed", color="gray") >> igw
    private_rt >> Edge(label="0.0.0.0/0", style="dashed", color="gray") >> nat_gateway

    # ========================================
    # CI/CD Integration with Infrastructure
    # ========================================
    codedeploy_app >> Edge(label="4. Deploy\n(appspec.yml)", color="darkblue", style="bold") >> app_server
    app_server >> Edge(label="Pull Docker Image", color="darkblue", style="dashed") >> ecr
    app_server >> Edge(label="Fetch Env Vars", color="darkblue", style="dashed") >> ssm
    app_server >> Edge(label="Send Logs", color="darkblue", style="dashed") >> cloudwatch
    s3_deploy >> Edge(label="Deployment Package", color="darkblue", style="dashed") >> codedeploy_app

    # ========================================
    # ALB Placement in Public Subnets
    # ========================================
    public_rt_a - Edge(style="invis") - alb
    public_subnet_c - Edge(style="invis") - alb

print("✅ Architecture diagram generated successfully!")
print("📊 Output file: tpt_dev_architecture.png")
print("\n📋 Diagram includes:")
print("  - VPC with 6 subnets across 2 AZs")
print("  - Internet Gateway and NAT Gateway")
print("  - Application Load Balancer (ALB)")
print("  - EC2 Instances (Bastion Host, App Server)")
print("  - RDS MySQL Database")
print("  - Security Groups and Route Tables")
print("  - CI/CD Pipeline (GitHub Actions → ECR → CodeDeploy)")
print("  - AWS Parameter Store & CloudWatch Logs")
print("  - Network traffic flows")
