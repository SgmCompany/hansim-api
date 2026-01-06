# hansim-api

## dev 환경 변수 준비
cp docker-compose/.env.example docker-compose/.env

## 빌드 & 기동
docker compose -f docker-compose/docker-compose.yaml -p hansim up -d --build

## 로그
docker compose -f docker-compose/docker-compose.yaml -p hansim logs -f app

## 앱
open http://127.0.0.1:8081

## DB
mysql -h 127.0.0.1 -P 3307 -u app -papp_pw hansim
