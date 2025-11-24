#!/usr/bin/env python3
"""
TradingPT Dev Server - CI/CD Pipeline Diagram
배포 파이프라인 흐름도 (한글 버전)
"""

from diagrams import Diagram, Cluster, Edge
from diagrams.onprem.vcs import Github
from diagrams.onprem.ci import GithubActions
from diagrams.aws.compute import EC2, ECR
from diagrams.aws.storage import S3
from diagrams.aws.devtools import Codedeploy
from diagrams.aws.management import SystemsManager, Cloudwatch
from diagrams.programming.language import Java

# 한글 폰트 설정
graph_attr = {
    "fontsize": "16",
    "fontname": "AppleGothic",
    "bgcolor": "white",
    "pad": "0.8",
    "nodesep": "1.2",
    "ranksep": "1.8",
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
    "TradingPT Dev Server\nCI/CD 배포 파이프라인",
    filename="tpt_dev_cicd_pipeline",
    direction="LR",
    graph_attr=graph_attr,
    node_attr=node_attr,
    show=False,
    outformat="png",
):

    # 1단계: 코드 푸시
    with Cluster("1단계: 소스 코드", graph_attr={"bgcolor": "#E8F5E9", **cluster_attr}):
        github = Github("GitHub Repository\n(develop 브랜치)")

    # 2단계: 빌드
    with Cluster("2단계: 빌드 & 테스트", graph_attr={"bgcolor": "#E3F2FD", **cluster_attr}):
        actions = GithubActions("GitHub Actions\n워크플로우")
        gradle = Java("Gradle 빌드\n./gradlew bootJar")

    # 3단계: Docker 이미지
    with Cluster("3단계: 컨테이너 이미지", graph_attr={"bgcolor": "#F3E5F5", **cluster_attr}):
        ecr = ECR("Amazon ECR\ntpt-server-dev\n(Docker Registry)")

    # 4단계: 배포 패키지
    with Cluster("4단계: 배포 준비", graph_attr={"bgcolor": "#FFF3E0", **cluster_attr}):
        s3 = S3("Amazon S3\ntpt-dev-deployments\n(배포 패키지)")
        codedeploy = Codedeploy("AWS CodeDeploy\ntpt-server-dev")

    # 5단계: 배포 실행
    with Cluster("5단계: 서버 배포", graph_attr={"bgcolor": "#FFEBEE", **cluster_attr}):
        ec2 = EC2("EC2 App Server\ntpt-dev-server-a\n(Docker Container)")
        ssm = SystemsManager("Parameter Store\n환경변수 로드")
        cloudwatch = Cloudwatch("CloudWatch Logs\n애플리케이션 로그")

    # 파이프라인 흐름
    github >> Edge(label="① push/merge", color="darkgreen", style="bold") >> actions
    actions >> Edge(label="② Build JAR", color="blue", style="bold") >> gradle
    gradle >> Edge(label="③ Build Docker\nImage", color="purple", style="bold") >> ecr

    actions >> Edge(label="④ Upload\n배포 패키지", color="orange", style="bold") >> s3
    s3 >> Edge(label="⑤ Trigger\nDeployment", color="red", style="bold") >> codedeploy

    codedeploy >> Edge(label="⑥ Deploy\n(appspec.yml)", color="darkred", style="bold") >> ec2
    ecr >> Edge(label="⑦ Pull Image", color="purple", style="dashed") >> ec2
    ssm >> Edge(label="⑧ Load Env", color="green", style="dashed") >> ec2
    ec2 >> Edge(label="⑨ Send Logs", color="brown", style="dotted") >> cloudwatch

print("✅ CI/CD 파이프라인 다이어그램 생성 완료!")
print("📊 출력 파일: tpt_dev_cicd_pipeline.png")
print("\n배포 단계:")
print("  ① GitHub push/merge → develop 브랜치")
print("  ② GitHub Actions 빌드 (Gradle)")
print("  ③ Docker 이미지 빌드 및 ECR 푸시")
print("  ④ 배포 패키지 (appspec.yml + scripts) S3 업로드")
print("  ⑤ CodeDeploy 배포 트리거")
print("  ⑥ EC2 인스턴스에 배포")
print("  ⑦ ECR에서 Docker 이미지 Pull")
print("  ⑧ Parameter Store에서 환경변수 로드")
print("  ⑨ CloudWatch Logs로 로그 전송")
