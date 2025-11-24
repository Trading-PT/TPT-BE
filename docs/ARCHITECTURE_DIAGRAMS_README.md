# TradingPT Dev Server - 아키텍처 다이어그램 가이드

## 📊 생성된 다이어그램

### 1. AWS 인프라 아키텍처 (Infrastructure)
**파일명**: `tpt_dev_architecture_clean.png` (245KB)

**포함 내용**:
- ✅ VPC 구조 (10.0.0.0/16)
- ✅ 가용 영역 2개 (ap-northeast-2a, ap-northeast-2c)
- ✅ 6개 서브넷 (Public 2개, App Private 2개, DB Private 2개)
- ✅ Internet Gateway & NAT Gateway
- ✅ Application Load Balancer (ALB)
- ✅ EC2 인스턴스 (Bastion Host, App Server)
- ✅ RDS MySQL Database
- ✅ 네트워크 트래픽 흐름

**특징**:
- 한글 폰트 (AppleGothic)
- 큰 글씨 크기 (14-16pt)
- 색상별 서브넷 구분:
  - 🟢 Public Subnet (연두색)
  - 🔵 App Private Subnet (하늘색)
  - 🟠 DB Private Subnet (주황색)
- 명확한 트래픽 흐름 (화살표 색상/스타일 구분)

---

### 2. CI/CD 배포 파이프라인 (Deployment Pipeline)
**파일명**: `tpt_dev_cicd_pipeline.png` (228KB)

**배포 단계 (좌→우 흐름)**:

#### 1단계: 소스 코드 (🟢)
- GitHub Repository (develop 브랜치)

#### 2단계: 빌드 & 테스트 (🔵)
- GitHub Actions 워크플로우
- Gradle 빌드 (bootJar)

#### 3단계: 컨테이너 이미지 (🟣)
- Amazon ECR (Docker Registry)

#### 4단계: 배포 준비 (🟠)
- Amazon S3 (배포 패키지 저장)
- AWS CodeDeploy (배포 오케스트레이션)

#### 5단계: 서버 배포 (🔴)
- EC2 App Server (Docker Container 실행)
- Parameter Store (환경변수 로드)
- CloudWatch Logs (로그 수집)

**특징**:
- 좌→우 시간순 배치
- 9단계 배포 프로세스 시각화
- 색상별 단계 구분
- 화살표 번호 매김 (①~⑨)

---

## 🎯 다이어그램 사용 가이드

### 인프라 다이어그램 활용
```
목적: AWS 리소스 배치 및 네트워크 구조 이해

활용 시나리오:
1. 신규 팀원 온보딩
2. 인프라 변경 계획 수립
3. 보안 그룹 규칙 검토
4. 비용 최적화 검토
5. 장애 대응 시나리오 계획
```

### CI/CD 다이어그램 활용
```
목적: 배포 프로세스 및 자동화 흐름 이해

활용 시나리오:
1. 배포 절차 교육
2. 배포 실패 트러블슈팅
3. 배포 자동화 개선
4. 환경변수 관리 검토
5. 로그 수집 프로세스 이해
```

---

## 📁 파일 구조

```
tpt-api/
├── tpt_dev_architecture_clean.png      # 인프라 다이어그램
├── tpt_dev_cicd_pipeline.png           # CI/CD 파이프라인
├── generate_clean_architecture_diagram.py
├── generate_cicd_pipeline_diagram.py
└── docs/
    ├── ARCHITECTURE_DIAGRAMS_README.md  # 이 문서
    └── AWS_INFRASTRUCTURE_COMPLETE.md   # 상세 인프라 문서
```

---

## 🔄 다이어그램 재생성

### 요구사항
```bash
# Python 패키지
pip3 install diagrams

# Graphviz (macOS)
brew install graphviz

# Graphviz (Ubuntu/Debian)
sudo apt-get install graphviz
```

### 생성 명령어
```bash
# 인프라 다이어그램
python3 generate_clean_architecture_diagram.py

# CI/CD 파이프라인
python3 generate_cicd_pipeline_diagram.py
```

---

## 📊 트래픽 흐름 설명

### 1. 사용자 → 애플리케이션
```
사용자/클라이언트
  ↓ (HTTPS/HTTP)
인터넷 게이트웨이
  ↓
로드 밸런서 (ALB)
  ↓ (HTTP:8080)
애플리케이션 서버 (Docker Container)
```

### 2. SSH 접근 (관리자)
```
관리자
  ↓ (SSH:22)
Bastion Host (점프 서버)
  ↓ (SSH:22)
애플리케이션 서버
```

### 3. 데이터베이스 연결
```
애플리케이션 서버
  ↓ (MySQL:3306)
RDS MySQL Database
```

### 4. 아웃바운드 인터넷 (Private → Internet)
```
애플리케이션 서버 (Private Subnet)
  ↓
NAT 게이트웨이 (Public Subnet)
  ↓
인터넷 게이트웨이
  ↓
인터넷 (외부 API, 패키지 다운로드)
```

### 5. 배포 프로세스
```
GitHub (develop 푸시)
  ↓
GitHub Actions (빌드)
  ↓
ECR (Docker 이미지 저장)
  ↓
S3 (배포 패키지 저장)
  ↓
CodeDeploy (배포 실행)
  ↓
EC2 App Server
  ├─ ECR에서 이미지 Pull
  ├─ Parameter Store에서 환경변수 로드
  └─ CloudWatch Logs로 로그 전송
```

---

## 🎨 색상 코드 가이드

### 인프라 다이어그램
| 색상 | 용도 | 의미 |
|------|------|------|
| 🟢 연두색 | Public Subnet | 인터넷 접근 가능 |
| 🔵 하늘색 | App Private Subnet | 애플리케이션 실행 영역 |
| 🟠 주황색 | DB Private Subnet | 데이터베이스 격리 영역 |
| 🔴 빨간색 | 인바운드 트래픽 | 사용자 → ALB → App |
| 🟣 보라색 | SSH 접근 | Bastion → App |
| 🟤 갈색 | DB 연결 | App → RDS |
| 🟢 초록색 | 아웃바운드 | App → NAT → Internet |

### CI/CD 파이프라인
| 색상 | 단계 | 의미 |
|------|------|------|
| 🟢 연두색 | 1단계 | 소스 코드 (GitHub) |
| 🔵 하늘색 | 2단계 | 빌드 & 테스트 |
| 🟣 보라색 | 3단계 | 컨테이너 이미지 (ECR) |
| 🟠 주황색 | 4단계 | 배포 준비 (S3, CodeDeploy) |
| 🔴 빨간색 | 5단계 | 서버 배포 (EC2, Logs) |

---

## 📝 업데이트 이력

### Version 2.0 (2025-11-24)
- ✅ 가독성 대폭 개선 (한글 폰트, 큰 글씨)
- ✅ 인프라 다이어그램과 CI/CD 파이프라인 분리
- ✅ 색상별 서브넷 명확히 구분
- ✅ 트래픽 흐름 화살표 색상/스타일 차별화
- ✅ 한글 라벨 적용 (AppleGothic 폰트)

### Version 1.0 (2025-11-24)
- 초기 통합 다이어그램 생성 (복잡도 높음)

---

## 🔗 관련 문서

- **상세 인프라 문서**: `docs/AWS_INFRASTRUCTURE_COMPLETE.md`
- **CI/CD 워크플로우**: `.github/workflows/deploy-dev.yml`
- **CodeDeploy 스펙**: `appspec.yml`
- **배포 스크립트**: `scripts/` 디렉토리

---

**Last Updated**: 2025-11-24
**Version**: 2.0
**Author**: Architecture Documentation System
