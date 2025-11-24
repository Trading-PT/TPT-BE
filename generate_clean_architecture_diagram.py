#!/usr/bin/env python3
"""
TradingPT Dev Server - Clean AWS Infrastructure Diagram
깔끔하고 가독성 좋은 인프라 다이어그램 (한글 버전)
"""

from diagrams import Diagram, Cluster, Edge
from diagrams.aws.compute import EC2
from diagrams.aws.network import ELB, InternetGateway, NATGateway
from diagrams.aws.database import RDS

# 한글 폰트 설정 및 다이어그램 속성
graph_attr = {
    "fontsize": "16",
    "fontname": "AppleGothic",
    "bgcolor": "white",
    "pad": "0.8",
    "nodesep": "1.0",
    "ranksep": "1.5",
    "splines": "ortho",
}

node_attr = {
    "fontsize": "14",
    "fontname": "AppleGothic",
}

cluster_attr = {
    "fontsize": "15",
    "fontname": "AppleGothic",
}

with Diagram(
    "TradingPT Dev Server\nAWS 인프라 아키텍처",
    filename="tpt_dev_architecture_clean",
    direction="TB",
    graph_attr=graph_attr,
    node_attr=node_attr,
    show=False,
    outformat="png",
):

    # 사용자 및 인터넷
    internet_users = EC2("사용자/클라이언트\n(인터넷)")

    # Internet Gateway
    igw = InternetGateway("인터넷 게이트웨이\nigw-xxx")

    # VPC
    with Cluster("VPC: tpt-dev-vpc\n(10.0.0.0/16)", graph_attr=cluster_attr):

        # Application Load Balancer (VPC 레벨에 배치)
        alb = ELB("로드 밸런서 (ALB)\ntpt-dev-alb\nHTTP/HTTPS")

        # 가용 영역 2a
        with Cluster("가용 영역\nap-northeast-2a", graph_attr={"bgcolor": "#FFF8DC", **cluster_attr}):

            # Public Subnet 2a
            with Cluster("Public Subnet\n10.0.1.0/24", graph_attr={"bgcolor": "#C8E6C9", **cluster_attr}):
                nat_gw = NATGateway("NAT 게이트웨이\nEIP: 13.209.232.146")
                bastion = EC2("Bastion Host\n(점프 서버)\n10.0.1.31")

            # App Private Subnet 2a
            with Cluster("App Private Subnet\n10.0.3.0/24", graph_attr={"bgcolor": "#BBDEFB", **cluster_attr}):
                app_server_a = EC2("애플리케이션 서버\ntpt-dev-server-a\n10.0.3.118\n(Docker/Spring Boot)")

            # DB Private Subnet 2a
            with Cluster("DB Private Subnet\n10.0.5.0/24", graph_attr={"bgcolor": "#FFE0B2", **cluster_attr}):
                db_placeholder_a = EC2("(RDS Subnet\nGroup 영역)")

        # 가용 영역 2c
        with Cluster("가용 영역\nap-northeast-2c", graph_attr={"bgcolor": "#F0F8FF", **cluster_attr}):

            # Public Subnet 2c
            with Cluster("Public Subnet\n10.0.2.0/24", graph_attr={"bgcolor": "#C8E6C9", **cluster_attr}):
                public_placeholder_c = EC2("(ALB 배치 영역)")

            # App Private Subnet 2c
            with Cluster("App Private Subnet\n10.0.4.0/24", graph_attr={"bgcolor": "#BBDEFB", **cluster_attr}):
                app_placeholder_c = EC2("(예비 영역)")

            # DB Private Subnet 2c
            with Cluster("DB Private Subnet\n10.0.6.0/24", graph_attr={"bgcolor": "#FFE0B2", **cluster_attr}):
                rds = RDS("RDS MySQL 8.0.42\ntpt-dev-db\ndb.t4g.micro\nPort: 3306")

    # 트래픽 흐름 - 사용자 → ALB
    internet_users >> Edge(label="HTTPS/HTTP", color="red", style="bold") >> igw
    igw >> Edge(label="인터넷 트래픽", color="red", style="bold") >> alb

    # ALB → App Server
    alb >> Edge(label="HTTP:8080\n(Target Group)", color="orange", style="bold") >> app_server_a

    # SSH 접근
    bastion >> Edge(label="SSH 접속", color="purple", style="dashed") >> app_server_a

    # DB 연결
    app_server_a >> Edge(label="MySQL:3306\n데이터베이스 쿼리", color="brown", style="bold") >> rds

    # NAT Gateway 아웃바운드
    app_server_a >> Edge(label="아웃바운드\n인터넷 접속", color="green", style="dotted") >> nat_gw
    nat_gw >> Edge(label="외부 API 호출", color="green", style="dotted") >> igw

print("✅ 깔끔한 아키텍처 다이어그램 생성 완료!")
print("📊 출력 파일: tpt_dev_architecture_clean.png")
print("\n주요 특징:")
print("  - 한글 폰트 사용 (AppleGothic)")
print("  - 큰 글씨 크기 (14-16pt)")
print("  - 색상별 서브넷 구분")
print("  - 명확한 트래픽 흐름")
print("  - CI/CD 요소 제거 (순수 인프라만)")
