# Bài 5 - Session 12: Chẩn đoán và khắc phục 3 sự cố Pipeline CI/CD

Pipeline `.github/workflows/broken-pipeline.yml` gồm 2 job: `test` (chạy `./gradlew test`) và `docker` (đăng nhập GHCR, build và push image, `needs: test`). Bản gốc bị lỗi (lưu ở `broken-pipeline.original.yml`) dính 3 sự cố sau.

## Lỗi 1: Permission Denied khi chạy Gradle Wrapper

**Triệu chứng.** Job `test` dừng ở bước "Chạy test" với log:

```
./gradlew: Permission denied
Error: Process completed with exit code 126.
```

**Nguyên nhân.** File `gradlew` được commit mà không có quyền thực thi (thường gặp khi tạo repo từ Windows, hoặc `git` ghi nhận file ở chế độ `100644`). Runner Ubuntu không chạy được file thiếu quyền `x`. Exit code 126 nghĩa là lệnh tồn tại nhưng không thực thi được.

**Cách sửa.** Thêm bước cấp quyền trước khi gọi Gradle:

```yaml
- name: Cấp quyền thực thi cho gradlew
  run: chmod +x ./gradlew
```

Cách sửa tận gốc ở máy cá nhân là `git update-index --chmod=+x gradlew` rồi commit.

## Lỗi 2: Authentication Failed khi đăng nhập Docker Registry

**Triệu chứng.** Bước "Đăng nhập GHCR" thất bại:

```
Error: Username and password required
```

hoặc `Error response from daemon: Get "https://ghcr.io/v2/": unauthorized`.

**Nguyên nhân.** Workflow truyền `${{ secrets.REGISTRY_TOKN }}` (sai chính tả), trong khi secret thật tên `REGISTRY_TOKEN`. GitHub Actions không báo lỗi khi tham chiếu secret không tồn tại, mà trả về chuỗi rỗng. Kết quả là `password` rỗng nên đăng nhập thất bại.

**Cách sửa.** Đúng tên secret:

```yaml
password: ${{ secrets.REGISTRY_TOKEN }}
```

Đồng thời kiểm tra secret `REGISTRY_TOKEN` đã tồn tại trong Settings, Secrets and variables, Actions (là token có quyền `write:packages`).

## Lỗi 3: Build Context Error, không tìm thấy Dockerfile

**Triệu chứng.** Bước "Build và push image" báo lỗi, dòng cuối có dạng:

```
ERROR: unable to prepare context: path "./app" not found
```

Nếu thư mục tồn tại nhưng không chứa Dockerfile thì lỗi là `failed to read dockerfile: open Dockerfile: no such file or directory`.

**Nguyên nhân.** Bản gốc đặt `context: ./app`. Thư mục `app` không tồn tại trong repo, còn `Dockerfile` thực tế nằm ở thư mục gốc, nên Docker Buildx không dựng được build context.

**Cách sửa.** Trỏ context về thư mục gốc và khai báo rõ đường dẫn Dockerfile:

```yaml
context: .
file: ./Dockerfile
```

## Kết quả

Sau khi sửa cả 3 lỗi và push commit mới, job `test` và `docker` đều tích xanh. Ảnh chụp tab Actions đi kèm bài nộp.
