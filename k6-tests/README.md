# TPT-API 부하 테스트 가이드

운영 서버 부하 테스트를 위한 k6 스크립트 모음입니다.

## 📋 사전 준비

### 1. 테스트 계정 생성

운영 DB에 접속하여 SQL 스크립트 실행:

```bash
# 운영 DB 접속
mysql -h <운영DB호스트> -u <유저> -p <데이터베이스명>

# 테스트 계정 1000개 생성
mysql> source sql/create-test-accounts.sql
```

생성되는 계정:
- 아이디: `loadtest_user_900001` ~ `loadtest_user_901000`
- 비밀번호: `loadtest123!`
- 상태: `ACTIVE`, `UID_APPROVED` (바로 사용 가능)

### 2. k6 설치 (EC2)

```bash
# Ubuntu/Debian
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6

# Amazon Linux 2
sudo yum install https://dl.k6.io/rpm/repo.rpm
sudo yum install k6
```

## 🧪 테스트 실행

### Step 1: 스모크 테스트 (필수)

본격적인 부하 테스트 전에 기본 동작 확인:

```bash
BASE_URL=https://api.tradingpt.kr k6 run production-smoke-test.js
```

- VU: 5명
- Duration: 1분
- 목적: 로그인 및 기본 API 동작 확인

**스모크 테스트가 PASS해야 본 테스트 진행!**

### Step 2: 본 부하 테스트

```bash
BASE_URL=https://api.tradingpt.kr k6 run production-load-test.js
```

시나리오:
- 0분~1분: 0 → 100 VU
- 1분~3분: 100 → 500 VU
- 3분~6분: 500 → 1000 VU 유지
- 6분~8분: 1000 → 500 VU
- 8분~9분: 500 → 0 VU

### Step 3: 결과 분석

JSON 리포트 생성:

```bash
BASE_URL=https://api.tradingpt.kr k6 run --out json=results.json production-load-test.js
```

## 📊 성능 임계값

| 메트릭 | 임계값 | 설명 |
|--------|--------|------|
| http_req_duration p(95) | < 1000ms | 95%의 요청이 1초 이내 |
| http_req_failed | < 5% | HTTP 에러율 5% 미만 |
| login_success_rate | > 95% | 로그인 성공률 95% 이상 |
| api_error_rate | < 5% | API 에러율 5% 미만 |

## 🧹 테스트 후 정리

**테스트 완료 후 반드시 테스트 데이터 정리:**

```bash
mysql -h <운영DB호스트> -u <유저> -p <데이터베이스명>

# 테스트 계정 삭제 (주석 해제 후 실행)
mysql> source sql/cleanup-test-accounts.sql
mysql> CALL cleanup_loadtest_accounts();
```

## 📁 파일 구조

```
k6-tests/
├── README.md                    # 이 파일
├── production-smoke-test.js     # 스모크 테스트 (기본 동작 확인)
├── production-load-test.js      # 본 부하 테스트 (1000 VU)
├── smoke-test.js               # 개발 서버용 스모크 테스트
├── load-test-example.js        # 개발 서버용 부하 테스트 (인증 우회)
└── sql/
    ├── create-test-accounts.sql    # 테스트 계정 생성 SQL
    └── cleanup-test-accounts.sql   # 테스트 계정 정리 SQL
```

## ⚠️ 주의사항

1. **운영 서버 부하 테스트는 사전 공지 후 진행**
2. **피크 시간대 피해서 테스트** (새벽 2-6시 권장)
3. **테스트 전 DB 백업 권장**
4. **테스트 후 반드시 테스트 계정 정리**
5. **모니터링 대시보드 확인하며 진행**

## 🔧 문제 해결

### 로그인 실패율이 높은 경우

1. 테스트 계정이 제대로 생성되었는지 확인
2. 비밀번호 해시가 올바른지 확인
3. DB 연결 풀 설정 확인 (HikariCP max-pool-size)

### 응답 시간이 느린 경우

1. DB 커넥션 풀 확장 고려
2. Redis 연결 상태 확인
3. JVM 힙 메모리 확인

### 세션 관련 에러

1. Redis 서버 상태 확인
2. 세션 스토어 용량 확인
3. 동시 세션 제한 설정 확인
