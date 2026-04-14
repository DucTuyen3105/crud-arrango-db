# Demo User Management (Spring Boot + ArangoDB)

Du an nay la mot ung dung CRUD nguoi dung, dung:
- Spring Boot (Web, Validation, Thymeleaf)
- ArangoDB Java Driver (`com.arangodb:arangodb-java-driver`)
- AQL de truy van du lieu

Muc tieu chinh: quan ly danh sach `users` trong ArangoDB va cung cap ca API JSON + giao dien web.

## 1) Kien truc tong quan

Luong xu ly request:

1. `Controller` nhan HTTP request
2. `Service` chua business logic (muc co ban)
3. `Repository` thuc thi AQL tren ArangoDB
4. Tra ket qua ve `Controller`

Folder chinh:

- `src/main/java/com/example/demo/config`: cau hinh ArangoDB
- `src/main/java/com/example/demo/user/controller`: REST API va page controller
- `src/main/java/com/example/demo/user/service`: service layer
- `src/main/java/com/example/demo/user/repository`: repository layer (AQL)
- `src/main/resources/application.properties`: bien cau hinh ket noi DB

## 2) Chay du an

Yeu cau:
- Java 17
- ArangoDB dang chay (thuong la Docker port `8529`)

Cau hinh mac dinh trong `application.properties`:

- `arango.host=localhost`
- `arango.port=8529`
- `arango.username=root`
- `arango.password=123456`
- `arango.database=user_management`

Lenh chay:

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

Mo frontend:

- `http://localhost:8080/`

## 3) API nhanh

Base path: `/api/users`

- `GET /api/users` - Lay danh sach user
- `GET /api/users/{id}` - Lay user theo `_key`
- `POST /api/users` - Tao user
- `PUT /api/users/{id}` - Cap nhat user
- `DELETE /api/users/{id}` - Xoa user

## 4) Giai thich ki phan Repository

File: `src/main/java/com/example/demo/user/repository/UserRepository.java`

Repository nay **khong dung Spring Data JPA**, ma truy van truc tiep ArangoDB bang AQL thong qua `ArangoDatabase`.

### 4.1 Vai tro cua cac thanh phan

- `ArangoDatabase arangoDatabase`: doi tuong lam viec voi 1 database cu the.
- `ArangoCursor<BaseDocument>`: con tro doc ket qua query theo stream.
- `BaseDocument`: document raw cua ArangoDB, sau do map ve model `User`.
- `AqlQueryOptions`: tuy chon bo sung cho query (hien tai dung mac dinh).
- `Map<String, Object> bindVars`: truyen bien vao AQL (`@key`, `@name`...) de:
  - tranh noi chuoi query truc tiep
  - giam loi va an toan hon
  - tach query va data ro rang

### 4.2 Vi sao map `BaseDocument` -> `User`

Arango tra du lieu dang document tong quat. Ham `toUser(BaseDocument doc)` lam:

- Lay `_key` qua `doc.getKey()` -> `id` trong app
- Lay `name`, `email`, `age` tu attributes
- Ep kieu `age` an toan (`Number -> int`)

Uu diem:
- Domain model (`User`) tach biet voi driver class
- De doi schema/driver trong tuong lai

### 4.3 Chi tiet tung method CRUD

#### `findAll()`

- AQL: `FOR u IN users SORT u._key DESC RETURN u`
- Khong can `bindVars`
- Duyet cursor, map tung document sang `User`
- Tra ve `List<User>`

#### `findById(String key)`

- AQL: filter theo `_key == @key`, `LIMIT 1`
- Dung `bindVars.put("key", key)`
- Neu cursor khong co ket qua -> `Optional.empty()`
- Neu co -> `Optional.of(user)`

Ly do dung `Optional`:
- Bieu dat ro du lieu co the khong ton tai
- Controller xu ly `404 Not Found` de dang

#### `create(User user)`

- AQL: `INSERT { ... } IN users RETURN NEW`
- Truyen `name`, `email`, `age` bang bind vars
- Lay document `NEW` vua tao tu cursor
- Map va tra ve user moi

#### `update(String key, User user)`

- AQL:
  - `UPDATE @key WITH { ... } IN users`
  - `OPTIONS { keepNull: false }`
  - `RETURN NEW`
- `keepNull: false` co nghia:
  - field nao = `null` se khong ghi de thanh `null` trong document (giu du lieu cu)
- Khong co ket qua -> `Optional.empty()`
- Co ket qua -> `Optional.of(user moi sau update)`

#### `deleteById(String key)`

- AQL:
  - Tim document theo `_key`
  - `REMOVE u IN users`
  - `RETURN OLD`
- Neu co document bi xoa -> cursor co ket qua (`true`)
- Neu khong tim thay -> `false`

### 4.4 Signature query hien tai cua driver

Driver dang dung signature:

```java
arangoDatabase.query(query, BaseDocument.class, bindVars, new AqlQueryOptions())
```

Thu tu quan trong:
1. `query string`
2. `result class`
3. `bind vars`
4. `query options`

Neu dat sai thu tu se loi compile.

### 4.5 Nhung diem can chu y khi mo rong Repository

- Luon dung bind vars thay vi noi chuoi vao AQL
- Dong nhat collection name (`users`) -> can nhac tach thanh constant
- Validate du lieu o layer request/service truoc khi vao repository
- Neu query lon, can bo sung index ben ArangoDB de tang toc do
- Can nhac phan trang (`LIMIT/OFFSET`) cho `findAll()`

## 5) ArangoConfig dang lam gi?

File: `src/main/java/com/example/demo/config/ArangoConfig.java`

- Tao bean `ArangoDB` (client)
- Tao bean `ArangoDatabase` (db dang dung)
- Trong `@PostConstruct`:
  - Thu tao database neu chua co
  - Thu tao collection `users` neu chua co
  - Neu ArangoDB chua san sang: log warning, khong lam app sap

Muc tieu la app van boot duoc, sau do ket noi DB khi san sang.

## 6) Frontend va template

- `UserPageController` map `/` -> template `users`
- Nghia la mo trinh duyet vao `http://localhost:8080/` de xem giao dien

## 7) Huong phat trien tiep

- Them test cho `UserRepository` (integration test voi Arango test container)
- Them phan trang + tim kiem user
- Tach DTO response/request ro hon
- Them logging/tracing cho query cham

