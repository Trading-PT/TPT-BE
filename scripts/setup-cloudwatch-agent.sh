#!/bin/bash
# =============================================================================
# CloudWatch Agent 설치 및 설정 스크립트
# 기존 EC2 인스턴스에 SSH 접속 후 실행
# =============================================================================

set -e

echo "=== CloudWatch Agent Setup Started at $(date) ==="

# CloudWatch Agent가 이미 설치되어 있는지 확인
if [ ! -f /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl ]; then
    echo "Installing CloudWatch Agent..."
    cd /tmp
    wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
    sudo dpkg -i -E ./amazon-cloudwatch-agent.deb
    rm -f ./amazon-cloudwatch-agent.deb
fi

# CloudWatch Agent 설정 파일 생성
echo "Creating CloudWatch Agent configuration..."
sudo tee /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json << 'EOF'
{
  "agent": {
    "metrics_collection_interval": 60,
    "run_as_user": "root",
    "logfile": "/opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log"
  },
  "metrics": {
    "namespace": "TradingPT/EC2",
    "metrics_collected": {
      "cpu": {
        "measurement": ["cpu_usage_idle", "cpu_usage_user", "cpu_usage_system", "cpu_usage_iowait"],
        "metrics_collection_interval": 60,
        "totalcpu": true,
        "resources": ["*"]
      },
      "mem": {
        "measurement": ["mem_used_percent", "mem_used", "mem_total", "mem_available", "mem_available_percent"],
        "metrics_collection_interval": 60
      },
      "disk": {
        "measurement": ["disk_used_percent", "disk_free", "disk_total"],
        "metrics_collection_interval": 60,
        "resources": ["/"]
      },
      "diskio": {
        "measurement": ["diskio_reads", "diskio_writes", "diskio_read_bytes", "diskio_write_bytes"],
        "metrics_collection_interval": 60,
        "resources": ["*"]
      },
      "swap": {
        "measurement": ["swap_used_percent", "swap_used", "swap_free"],
        "metrics_collection_interval": 60
      },
      "netstat": {
        "measurement": ["netstat_tcp_established", "netstat_tcp_time_wait"],
        "metrics_collection_interval": 60
      }
    },
    "append_dimensions": {
      "AutoScalingGroupName": "${aws:AutoScalingGroupName}",
      "InstanceId": "${aws:InstanceId}",
      "InstanceType": "${aws:InstanceType}"
    },
    "aggregation_dimensions": [
      ["AutoScalingGroupName"],
      ["InstanceId"],
      ["AutoScalingGroupName", "InstanceId"]
    ]
  },
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/home/ubuntu/app/logs/*.log",
            "log_group_name": "/tpt/prod/application",
            "log_stream_name": "{instance_id}/application",
            "retention_in_days": 14
          }
        ]
      }
    }
  }
}
EOF

# CloudWatch Agent 시작
echo "Starting CloudWatch Agent..."
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json \
    -s

# CloudWatch Agent 자동 시작 설정
sudo systemctl enable amazon-cloudwatch-agent

# 상태 확인
echo "=== CloudWatch Agent Status ==="
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl -a status

echo "=== CloudWatch Agent Setup Completed at $(date) ==="
echo ""
echo "메트릭은 CloudWatch > Metrics > TradingPT/EC2 네임스페이스에서 확인 가능합니다."
echo "약 1-2분 후부터 메트릭이 수집되기 시작합니다."
