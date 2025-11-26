#!/bin/bash
set -e

exec > >(tee /var/log/user-data.log|logger -t user-data -s 2>/dev/console) 2>&1
echo "=== User Data Script Started at $(date) ==="

apt-get update -y
apt-get upgrade -y

apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    unzip \
    ruby-full \
    wget

# Docker 설치 (공식 방법)
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
rm get-docker.sh
systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu

# AWS CLI v2 설치
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -q awscliv2.zip
./aws/install
rm -rf aws awscliv2.zip

# SSM Agent 설치 (Ubuntu용)
snap install amazon-ssm-agent --classic
systemctl enable snap.amazon-ssm-agent.amazon-ssm-agent.service
systemctl start snap.amazon-ssm-agent.amazon-ssm-agent.service

for i in {1..30}; do
    if systemctl is-active --quiet snap.amazon-ssm-agent.amazon-ssm-agent.service; then
        echo "SSM Agent is running"
        break
    fi
    sleep 2
done

# CodeDeploy Agent 설치
cd /tmp
wget -q https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install
chmod +x ./install
./install auto
systemctl start codedeploy-agent
systemctl enable codedeploy-agent

for i in {1..30}; do
    if systemctl is-active --quiet codedeploy-agent; then
        echo "CodeDeploy Agent is running"
        break
    fi
    sleep 2
done

# ============================================
# CloudWatch Agent 설치 및 설정 (추가된 부분)
# ============================================
echo "=== Installing CloudWatch Agent ==="
wget -q https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb -O /tmp/amazon-cloudwatch-agent.deb
dpkg -i /tmp/amazon-cloudwatch-agent.deb || apt-get install -f -y
rm /tmp/amazon-cloudwatch-agent.deb

# CloudWatch Agent 설정 파일 생성
mkdir -p /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.d
cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.d/file_amazon-cloudwatch-agent.json << 'CWCONFIG'
{
    "logs": {
        "logs_collected": {
            "files": {
                "collect_list": [
                    {
                        "file_path": "/var/log/aws/codedeploy-agent/codedeploy-agent.log",
                        "log_group_name": "/tpt/prod/codedeploy",
                        "log_stream_name": "{instance_id}/codedeploy-agent"
                    },
                    {
                        "file_path": "/opt/codedeploy-agent/deployment-root/deployment-logs/codedeploy-agent-deployments.log",
                        "log_group_name": "/tpt/prod/codedeploy",
                        "log_stream_name": "{instance_id}/deployments"
                    }
                ]
            }
        }
    }
}
CWCONFIG

# CloudWatch Agent 시작
/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a fetch-config -m ec2 -s -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.d/file_amazon-cloudwatch-agent.json
systemctl enable amazon-cloudwatch-agent

for i in {1..30}; do
    if systemctl is-active --quiet amazon-cloudwatch-agent; then
        echo "CloudWatch Agent is running"
        break
    fi
    sleep 2
done
# ============================================

mkdir -p /home/ubuntu/tpt-server
chown -R ubuntu:ubuntu /home/ubuntu/tpt-server

fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' | tee -a /etc/fstab

echo "=== User Data Script Completed at $(date) ==="
systemctl status codedeploy-agent --no-pager || true
systemctl status amazon-cloudwatch-agent --no-pager || true
