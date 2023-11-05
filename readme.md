---
created: 05/11/2023
modified: 05/11/2023
---

## Tổng Quan Về Bài Làm

- MSSV: 20120356
- Họ và tên: Lê Minh Quân
- Ngôn ngữ lập trình: Java
- Máy thực hiện demo: Kali Linux 12 core, 16GB RAM

## Kiến Trúc Chương Trình

Lớp `Process` đại diện cho một process và bao gồm các thành phần chính:
- `Server`: với mỗi remote port thì nó sẽ tạo ra một thread để lắng nghe các message. Điều này giúp cho chương trình không bị block khi lắng nghe các message từ nhiều process.
- `ClientHandler`: chịu trách nhiệm deliver và buffer message cũng như là cập nhật đồng hồ.
- `Client`: xây dựng và gửi message.

Khi khởi chạy hàm `main` ở trong `Process`, nó sẽ thực hiện các bước sau:
- Đọc tham số dòng lệnh được truyền vào.
- Tải cấu hình từ file (`configs.yaml`) thông qua lớp `Configuration`.
- Khởi tạo một instance của `Server` để lắng nghe message.
- Chạy các kịch bản tương ứng với tham số dòng lệnh. Có 2 kịch bản:
	- Demo: chạy 10 process và gửi 150 message.
	- Example: giả lập ví dụ về SES.

Chương trình còn có thêm 1 process có port là 0 nhằm quản lý các process khác. Nhiệm vụ của process này là:
- Gửi message thông báo cho phép các process kết nối lẫn nhau.
- Gửi message thông báo cho phép các process gửi message cho nhau.
- Bằng cách này, chúng ta có thể đảm bảo rằng tất cả n process đều sẵn sàng và đều kết nối đến n - 1 process còn lại trước khi gửi tin.

## Video Demo

- https://www.youtube.com/watch?v=e8dkj2SX3s4

## Tham Khảo

- [johannmeyer/Schiper-Eggli-Sandoz](https://github.com/johannmeyer/Schiper-Eggli-Sandoz)
- [Java Tutorials - Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html)
- [Java Tutorials - All About Sockets](https://docs.oracle.com/javase/tutorial/networking/sockets/index.html)
- [Java Socket Programming - Multiple Clients Chat](https://www.youtube.com/watch?v=gLfuZrrfKes&si=YCxMnrDcvwxOykcC)
