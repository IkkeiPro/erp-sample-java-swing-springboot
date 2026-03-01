
### posgreSQLのdocker作成
```
docker compose up -d
docker compose ps
```

### posgreSQLのdocker起動
```
docker exec -it swingapp-postgres psql -U swinguser -d swingapp -c "select 1;"
```

### Spring Bootの起動
```
mvn spring-boot:run
```
