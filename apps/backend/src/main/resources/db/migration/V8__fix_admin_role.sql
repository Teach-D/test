-- 기존에 USER로 생성된 "admin" 계정을 ADMIN으로 변경
UPDATE members SET role = 'ADMIN' WHERE LOWER(nickname) = 'admin' AND role = 'USER';
