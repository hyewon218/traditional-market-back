# 우리동네 전통시장 👨🏻‍🌾 🥕

## 📑 포트폴리오 
[노션 포트폴리오](https://ethereal-brie-237.notion.site/62192a2812804c8e8b29b04ee52f4442)<br>

## 📢 프로젝트 소개
소비자가 직접 시장에 가지 않고 구매하려는 상품에 대한 가격 정보를 홈페이지에서 편리하게 확인할 수 있습니다.<br>
현재는 각 점포에서 판매중인 상품에 대한 가격 정보만 게시하지만, 추후 주문을 통해 집으로 배달하는 기능 또한 고려하고 있습니다.

## ⁉️ 주제 선정 이유
전통시장에는 수많은 점포들이 대부분 겹치는 품목의 상품들을 판매하고 있기 때문에 각 점포들에서 판매하고 있는 상품들에 대한 가격 정보를 홈페이지에서 편하게 확인할 수 있다면 소비자 입장에서 비교를 통해 합리적인 소비를 할 수 있다고 생각해서 이 주제를 선택했습니다.

## ✅ 사용 기술 및 개발 환경
### Backend
- Java
- Spring Boot, Spring Data JPA, Spring Security
- JPA, Querydsl
- Gradle

### Infra
- AWS EC2, S3
- MySQL
- Redis
- Kafka
- Jenkins
- Docker
- Nginx
- DBeaver
- k6
- JMeter
- nGrinder

## ✅ Architecture
<img src="https://github.com/user-attachments/assets/b824b6e2-0a6e-4f8f-8f90-137f6bc8d034" width="90%"/><br>

## ✅ ERD
<img src="https://github.com/user-attachments/assets/810c45ff-b0f8-42d2-9487-8357bc5ca90b" width="90%"/><br>
### Used RDBMS
**MySQL**
### Index


## ✅ 주요 기능
### 💁🏻‍ 사용자
#### 비로그인 시
- 회원가입
- 로그인
- 시장, 상점, 상품 조회
  - 시장, 상점, 상품 좋아요 수 조회
  - 상품 댓글 조회
- 시장 길찾기
- 시장 내 상품 랭킹 조회(상품 가격 낮은 순 top 5 조회)
- 공지사항 조회

#### 로그인 시
- 로그아웃
- 내정보
  - 내정보 수정
    - 닉네임 변경
    - 비밀번호 변경
    - 탈퇴
  - 구매목록(주문내역)
  - 문의내역
    - 문의사항 조회
  - 배송지 관리
    - 배송지 추가 / 수정 / 삭제 / 조회
- 시장, 상점, 상품 조회
- 시장, 상점, 상품 좋아요 수 조회 / 좋아요 / 좋아요 취소
- 상품에 댓글 작성 / 수정 / 삭제 / 조회
- 다른 사용자의 댓글 신고
- 시장 길찾기
- 시장 내 상품 랭킹 조회(상품 가격 낮은 순 top 5 조회)
- 공지사항 조회
- 상품 장바구니 추가
- 장바구니 내 상품 조회 / 삭제 / 수량 수정
- 상품 주문 및 구매
- 문의사항 작성 / 조회
- 1 : 1 채팅 상담
- 알림
  - 채팅 시 관리자들에게 알람
  - 문의사항 남기면 관리자에게 알람
  - 상품 댓글 시 상품의 판매자에게 알람
    - 판매자 번호가 등록되어 있지 않으면 관리자에게 알람
  - 상품 좋아요 시 상품의 판매자에게 알람
    - 판매자 번호가 등록되어 있지 않으면 관리자에게 알람
  - 상점 좋아요 시 상점의 판매자에게 알람
    - 상점 판매자 번호가 등록되어 있지 않으면 관리자에게 알람


### 🧑🏻‍💻 관리자
- 홈페이지 현황
  - 회원 수 조회
  - 매출액 조회
  - 방문자 수 조회
- 회원 관리
  - 탈퇴회원 관리
  - 회원 제재
  - 회원 권한 변경
  - 회원 강제 탈퇴
- 주문 관리
  - 주문 상태 변경
- 시장, 상점, 상품 관리
  - 시장, 상점, 상품 조회 / 생성 / 수정 / 삭제
  - 시장, 상점, 상품 좋아요 수 조회
- 시장 길찾기
- 시장 내 상품 랭킹 조회(상품 가격 낮은 순 top 5 조회)
- 공지사항 관리
  - 공지사항 생성 / 수정 / 삭제
- 문의사항 관리
  - 문의사항 답변
- 1 : 1 채팅 상담 답변
- 알림
  - 채팅 상담 답변 시 채팅방 만든 사용자에게 알람
  - 문의사항 답변 시 문의사항 남긴 사용자에게 알람
- 회원 제재

### 👨🏻‍🌾 판매자
- 시장, 상점, 상품 조회
- 상점 주문 조회 / 상태 변경
- 상점, 상품 매출액 조회
- 상품 수정
- 알림
  - 사용자가 상품 댓글 시 상품의 판매자에게 알람
  - 사용자가 상품 좋아요 시 상품의 판매자에게 알람
  - 사용자가 상점 좋아요 시 상점의 판매자에게 알람

## ✅ 화면 UI
#### 일반 사용자 UI
| 회원가입                                                                                                            | 모바일                                                                                                           | 
|-----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/cc0241c0-f996-4c06-95ab-8d801dc1da9d" width="1250px"/><br> | <img src="https://github.com/user-attachments/assets/d604097f-bdc4-46d8-a7f7-7d9fe1890221" width="350px"/><br> | 


| 로그인                                                                                                             | 모바일                                                                                                            | 
|-----------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/dd6edd1c-29bb-4e3c-8087-7864d2aa3d4b" width="1250px"/><br> | <img src="https://github.com/user-attachments/assets/2b35593e-8c5c-4f71-9554-d7eb60dcc230" width="350px"/><br> | 


| 시장 조회                                                                                                           | 모바일                                                                                                           | 
|-----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/7e5c408f-cde3-42e3-9ca9-71d5bfb9d371" width="1250px"/><br> | <img src="https://github.com/user-attachments/assets/891782c0-2b4b-4006-b461-8fc2351b4611" width="350px"/><br> | 

| 시장 상세 조회                                                                                                        | 모바일                                                                                                                                                                                                                  | 
|-----------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/a325d26a-1eac-4238-aaec-37f92d7f83cc" width="2000px"/><br> | <img src="https://github.com/user-attachments/assets/34a77834-b89a-497a-9290-2a02b9ba85b8" width="280px"/><img src="https://github.com/user-attachments/assets/567c88c3-6e09-4e32-9240-4c28902f5b81" width="280px"/> | 

| 상점 상세 조회                                                                                                        | 모바일                                                                                                                                                                                                                  | 
|-----------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/72a66ffe-8724-4668-98e1-b2f24c4627e5" width="2000px"/><br> | <img src="https://github.com/user-attachments/assets/09a1368d-3d08-4afd-87ad-3d6083d54dd0" width="280px"/><img src="https://github.com/user-attachments/assets/9d4b8683-8a77-4d35-a7c7-a03ecf74cde4" width="280px"/> | 

| 상품 상세 조회                                                                                                        | 모바일                                                                                                                                                                                                               | 
|-----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/ecdcf1bc-a6e3-41e6-82ff-37e4e0aeeeed" width="1250px"/><br> | <img src="https://github.com/user-attachments/assets/6092ba0b-a77a-46b6-8c8a-20196bb6b554" width="350px"/>| 

| 장바구니                                                                                                            | 모바일                                                                                                                                                                                                              | 
|-----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/a24e1283-2271-4745-ad63-66db83443c26" width="1250px"/><br> | <img src="https://github.com/user-attachments/assets/9b30bcfc-9bf4-495b-ae6f-742eef7ee519" width="350px"/>| 


| 주문 및 결제                                                                                                                                                                                                                | 모바일                                                                                                                                                                                                               | 
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/b9ad26ef-ad5b-45b7-b401-9fd096f55066" width="1020px"/><img src="https://github.com/user-attachments/assets/48454808-ef6c-4dfb-ad02-11ff9cd6b1a3" width="1020px"/> | <img src="https://github.com/user-attachments/assets/bbe8beba-25c2-4b3e-9254-95fd5b33407d" width="280px"/><img src="https://github.com/user-attachments/assets/a6697de2-ee81-4098-a081-af12c26b86d2" width="280px"/> | 

| 배송지                                                                                                            | 모바일                                                                                                       | 
|----------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/a2a1d71b-c779-4fbf-b83e-826f66273bc3" width="1250px"/><br> | <img src="https://github.com/user-attachments/assets/1e0c708d-c08a-47e6-b348-a10854238035" width="350px"/> | 

| 결제 완료 후 주문 정보                                                                                                                                                                                                          | 모바일                                                                                                                                                                                                              | 
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/bc9884b9-4e08-4122-ab09-04e4f8f2f71d" width="1020px"/><img src="https://github.com/user-attachments/assets/abf68d6f-59bd-4600-a955-364c51a815ff" width="1020px"/> | <img src="https://github.com/user-attachments/assets/a492668f-1a0a-4a66-b02e-701c73f3d28f" width="280px"/><img src="https://github.com/user-attachments/assets/6bef776d-1ccf-4102-877f-c9d91e292d7f" width="280px"/>| 


| 채팅                                                                                                            |
|---------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/d3281554-62fd-469c-b0ad-2c8ae08f3703" width="100%"/><br> |

| 알람                                                                                                            |
|---------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/user-attachments/assets/b55f4529-289f-4955-b56c-20d6b340d75a" width="100%"/><br> |


## ✅ 기능별 비즈니스 로직
<details>
  <summary>회원가입</summary>

1. 회원가입 시 이메일과 아이디, 닉네임, 비밀번호를 입력합니다.
2. 이메일을 입력 후 해당 이메일로 인증번호를 보내 전송된 인증번호로 인증을 합니다.
3. 이메일과 아이디에 대해서 중복 확인을 합니다.
4. 아이디와 닉네임에 대해서 비속어가 있는지 확인합니다.
5. 비밀번호는 암호화를 거쳐 DB에 저장합니다.
</details>

<details>
  <summary><b>로그인</b></summary>

1. 아이디와 비밀번호를 이용해 로그인을 합니다.
2. 간편한 로그인을 위해 구글과 네이버, 카카오를 이용한 소셜로그인을 지원합니다.
3. 아이디와 비밀번호를 잊어버렸을 경우를 대비해 아이디 찾기와 임시비밀번호를 지원합니다.
</details>

<details>
  <summary><b>로그아웃</b></summary>

1. 로그아웃 버튼을 눌러 로그아웃을 할 수 있습니다.
</details>

<details>
  <summary><b>공지사항</b></summary>

1. 관리자가 작성한 공지사항에 대해 회원, 비회원 모두 열람 가능합니다.
</details>

<details>
  <summary><b>광고</b></summary>

1. 광고 구역을 추가해 사이트에 접속한 사람들에게 광고를 노출합니다.
</details>

<details>
  <summary><b>시장</b></summary>

1. 시장 생성 및 수정 시 좌표 검색을 이용해 근처 주차장 및 대중교통 정보를 입력하고 이미지를 추가할 수 있습니다.
2. 초기엔 전체 시장 목록을 로드하고, 이후 검색 또는 지역별 카테고리를 통해 원하는 시장을 조회할 수 있습니다.
3. 특정 시장을 클릭할 경우, 해당 시장의 정보와 시장에 소속된 상점 목록을 조회합니다.
4. `상품별 가격 순위` 버튼을 클릭할 경우, 해당 시장 내에서 상품별로 어느 가게가 가장 저렴한지 1위 부터 5위까지 확인할 수 있습니다.
5. 해당 시장에 대해 `좋아요` 버튼을 만들어 사람들이 선호하는 시장을 확인할 수 있습니다.
6. 해당 시장 근처 주차장, 대중교통 정보, 원하는 위치에서 해당 시장까지 가는 방법을 확인할 수 있습니다.
7. 해당 시장의 날씨 정보를 확인할 수 있습니다.
8. 상점 분류 카테고리를 통해 원하는 상점을 쉽게 찾을 수 있습니다.
</details>

<details>
  <summary><b>상점</b></summary>

1. 시장 생성 및 수정 시 좌표 검색을 이용해 근처 주차장 및 대중교통 정보를 입력하고 이미지를 추가할 수 있습니다.
2. 특정 상점을 클릭할 경우, 해당 상점의 정보와 상점에서 파는 상품들을 조회할 수 있습니다.
3. `좋아요` 기능을 통해 선호하는 상점을 확인할 수 있습니다.
4. 해당 상점의 위치를 확인할 수 있습니다.
</details>

<details>
  <summary><b>상품</b></summary>

1. 상품 생성 및 수정 시 이미지를 추가할 수 있습니다.
2. 특정 상품을 클릭할 경우, 해당 상품의 정보를 조회할 수 있습니다.
3. 해당 상품에 대해 장바구니에 담을 수 있고 바로 구매하기 또한 가능합니다.
4. `좋아요` 기능을 통해 선호하는 상품을 확인할 수 있습니다.
5. 댓글 기능을 통해 해당 상품에 대한 소비자들의 의견을 확인할 수 있습니다.
6. (회원만 가능) 댓글 신고 기능을 통해 다른 사용자에게 불쾌한 경험을 제공할 경우 해당 회원을 신고할 수 있습니다.
</details>

<details>
  <summary><b>내정보</b></summary>

1. 본인의 상세정보를 보기 전, 본인의 비밀번호를 입력해 본인이 맞는지 확인합니다.
2. 비밀번호 인증 후에는 비밀번호 확인 없이 상세정보를 조회할 수 있습니다.
3. 닉네임 변경은 30일에 한 번만 가능합니다.
4. 닉네임 변경까지 남은 시간을 확인할 수 있습니다.
5. 비밀번호 변경 시 현재 비밀번호를 입력하고 일치하면 원하는 비밀번호를 입력해 비밀번호를 변경합니다.
6. 회원 탈퇴 시엔 회원가입 시 입력한 이메일로 인증번호를 전송하고 전송된 인증번호를 입력 후 일치하면 탈퇴를 진행합니다.
</details>

<details>
  <summary><b>구매목록</b></summary>

1. 회원이 주문한 상품들의 구매내역을 조회합니다.
2. 전체 내역과 취소 내역을 구분해 조회할 수 있습니다.
3. 주문한 상품이 배송 전이라면 취소 사유를 입력하고 회원이 직접 취소 할 수 있습니다.
4. 배송이 완료되면 회원이 직접 구매확정을 할 수 있습니다.
</details>

<details>
  <summary><b>문의내역 및 답변</b></summary>

1. 관리자에게 이미지를 첨부할 수 있는 게시글 형태의 문의를 할 수 있습니다.
2. 문의하기 악용 및 의도와 다른 사용을 방지하기 위해 일일 5개 제한합니다.
3. 관리자는 확인 후 해당 문의하기에 대한 이미지를 첨부할 수 있는 답변을 할 수 있습니다.
4. 비속어를 포함하지 않도록 합니다.
</details>

<details>
  <summary><b>알람</b></summary>

1. 채팅 시 관리자들에게 알람이 전송됩니다.
2. 채팅 답변 시 사용자에게 알람이 전송됩니다.
3. 문의사항 남기면 관리자에게 알람이 전송됩니다.
4. 문의하기에 답변이 등록되면 사용자에게 알람이 전송됩니다.
5. 상품 • 상점 좋아요 시 판매자에게 알람이 전송됩니다.(판매자 번호가 등록되어 있지 않으면 관리자에게 알람이 전송됩니다.)
6. 상품 댓글 시 상품의 판매자에게 알람이 전송됩니다.
7. 알람 아이콘 옆에는 읽지 않은 알람 수가 나타나게 됩니다.
8. 알람 아이콘, 알람 메뉴를 통해 알람 페이지로 이동하여 알람 목록을 확인할 수 있습니다.
9. 알람 목록을 클릭하면 해당 페이지로 이동하게 되고 읽음으로 변경되어 배경이 노란색에서 흰색으로 변경됩니다.

</details>

<details>
  <summary><b>1:1 채팅상담</b></summary>

1. 상단바에서 1:1 채팅상담을 클릭하면 채팅 상담 목록이 나타나게 됩니다.
2. 사용자는 채팅 상담하기 버튼을 클릭하여 채팅방으로 이동하게 됩니다.
3. 사용자가 관리자에게 웹사이트에 대한 전반적인 궁금증을 물어볼 수 있습니다.(비속어를 포함하지 않도록 합니다.)
4. 관리자에게 1:1 채팅 상담 요청 알람이 도착하게 됩니다.
5. 사용자와 관리자가 실시간 채팅 상담이 가능합니다.
</details>

<details>
  <summary><b>방문자 조회</b></summary>

1. 일일 방문자와 총 방문자를 집계합니다.
</details>

<details>
  <summary><b>탈퇴회원</b></summary>

1. 탈퇴한 회원의 반복적인 재가입을 막기 위해 이메일, 아이디, IP 주소를 DB에 30일 간 저장 후 자동 파기합니다.
</details>

<details>
  <summary><b>제재</b></summary>

1. 다른 사용자에게 신고를 많이 받거나 웹사이트의 규정을 위반한 경우 회원 제재 기능을 통해 해당 회원이 문의하기를 제외한 댓글 작성, 일대일 채팅 상담에 대해 30일간 작성하지 못 하도록 합니다.
</details>

<details>
  <summary><b>장바구니</b></summary>

1. 구입할 상품을 담고 수량을 조절할 수 있습니다.
2. 정한 수량만큼 상품의 재고를 감소합니다.
</details>

<details>
  <summary><b>주문</b></summary>

1. 기존에 저장되어 있는 배송지를 선택하거나, 배송지를 추가할 수 있습니다.
2. 배송메시지를 선택 및 추가할 수 있습니다.
3. 구입할 상품에 대한 정보를 확인할 수 있습니다.
4. 결제 수단을 선택할 수 있습니다. 현재는 카카오페이 API만을 이용했습니다.
5. 이후 결제하기를 클릭하면 카카오페이 API를 호출해 결제를 진행합니다.
6. 주문한 수량만큼 상품의 재고를 감소합니다. 장바구니를 거치지 않고 상품 페이지에서 바로 주문한 경우에만 해당합니다.
</details>

<details>
  <summary><b>관리자 페이지</b></summary>

1. 관리자 권한만 접근 가능하며 홈페이지 현황, 회원 관리, 주문 관리, 시장 관리, 상점 관리, 상품 관리, 문의사항 관리, 공지사항 관리로 구성되어 있습니다.
2. 홈페이지 현황을 클릭하면 총 회원 수, 가입경로별 회원 수, 총 시장, 상점 수, 전체 시장 매출액, 오늘 및 전체 방문자 수, 시장별 상점 수 및 매출액 등 웹사이트의 전반적인 개요를 확인할 수 있습니다.
3. 회원 관리 페이지에서는 가입한 회원 목록을 조회합니다.
4. 회원 목록에서 특정 회원을 클릭하면 해당 회원의 상세정보를 조회하고 권한 변경, 제재 실행 및 제재 해제, 회원 강제 탈퇴를 할 수 있습니다.
5. 만약 관리자 회원을 클릭할 경우엔 권한 수정, 제재 실행, 회원 강제 탈퇴가 불가능합니다.
6. 판매자 회원의 경우엔 해당 회원이 소유하고 있는 상점 목록을 조회합니다.
7. 주문 관리 페이지에서는 회원이 상품을 주문한 주문 목록을 조회합니다.
8. 전체보기, 취소목록 또는 상품준비중, 배송중 등 주문 상태에 따른 조회가 가능하고 주문 상태를 변경할 수 있습니다.
9. 주문번호 및 아이디를 이용해 주문을 검색할 수 있습니다.
10. 시장 관리 페이지에서는 전체 시장 목록을 조회하고 시장 추가, 시장 검색, 지역별 조회를 할 수 있습니다.
11. 시장 목록에서 특정 시장을 클릭하면 해당 시장의 상세정보를 조회, 수정 및 삭제할 수 있고 상점 목록을 조회합니다.
12. 상점 관리 페이지에서는 전체 상점 목록을 조회하고 상점 추가, 상점 검색, 상점 분류별 조회를 할 수 있습니다.
13. 상점 목록에서 특정 상점을 클릭하면 해당 상점의 상세정보를 조회, 수정 및 삭제할 수 있고 상품 목록을 조회합니다.
14. 상품 관리 페이지에서는 전체 상품 목록을 조회하고 상품 추가, 상품 검색, 상품 분류별 조회를 할 수 있습니다.
15. 상품 목록에서 특정 상품을 클릭하면 해당 상품의 상세정보를 조회, 수정 및 삭제할 수 있고 댓글 목록을 조회합니다.
16. 문의사항 관리 페이지에서는 회원들이 문의한 목록을 조회하고 해당 문의사항에 들어가 답변을 할 수 있습니다.
17. 공지사항 관리 페이지에서는 작성된 공지사항 목록을 조회하고 공지사항 생성 및 수정, 삭제를 할 수 있습니다.
</details>

<details>
  <summary><b>판매자 페이지</b></summary>

1. 판매자 권한만 접근 가능하며 주문 관리, 상점 관리로 구성되어 있습니다.
2. 주문 관리 페이지에서는 회원이 상품을 주문한 주문 목록을 조회하고 관리자 주문 관리 페이지와 마찬가지로 주문 상태에 따른 조회 및 변경이 가능합니다. (주문 취소는 판매자의 실수를 방지하기 위해 관리자만 가능합니다.)
3. 주문 관리 페이지에서 조회되는 주문 목록은 해당 판매자가 소유하고 있는 상점의 상품을 포함하고 있는 주문만을 출력합니다.
4. 상점 관리 페이지에서는 해당 판매자가 소유하고 있는 상점 목록을 조회합니다.
5. 상점 목록에서 특정 상점을 클릭하면 해당 상점의 상세정보를 조회 및 수정할 수 있고 상품 목록을 조회합니다. 상품 추가도 가능합니다.
6. 조회한 상품 목록에서 특정 상품을 클릭하면 상품의 정보를 조회 및 수정할 수 있습니다.
7. 판매자와 관리자 간의 정보의 정합성을 위해 상점 추가는 관리자만 가능합니다.
</details>


## ✅ Git-Flow 브랜치 전략
Git-Flow 브랜치 전략에 따라 기능별로 브랜치를 나누어 작업하고 있고 모든 브랜치에 대해 pull request 를 통한 리뷰 완료 후 Merge 를 하고 있습니다.
- main : 제품으로 출시될 수 있는 브랜치
- develop : 다음 출시 버전을 개발하는 브랜치. feature 에서 리뷰완료한 브랜치를 Merge
- feature : 기능을 개발하는 브랜치


## ✅ 프로젝트를 진행하며 고민한 Technical Issue
1. [특정 시장에서 최소 가격 5개 조회 API index, redis 사용하여 조회 성능 개선](https://velog.io/@hyewon0218/%EC%84%B1%EB%8A%A5%EA%B0%9C%EC%84%A0-%ED%8A%B9%EC%A0%95-%EC%8B%9C%EC%9E%A5%EC%97%90%EC%84%9C-%EC%B5%9C%EC%86%8C-%EA%B0%80%EA%B2%A9-5%EA%B0%9C-%EC%A1%B0%ED%9A%8C)<br>
2. [상품 주문 시 비관적 Lock 으로 동시성 제어](https://velog.io/@hyewon0218/%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-%EC%83%81%ED%92%88-%EC%A3%BC%EB%AC%B8-%EC%8B%9C-%EB%B9%84%EA%B4%80%EC%A0%81-Lock%EC%9C%BC%EB%A1%9C-%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%A0%9C%EC%96%B4)<br>
3. [Redis + @Cacheable + @CacheEvict 이용하여 시장, 상점 조회 성능 개선 및 데이터 동기화](https://velog.io/@hyewon0218/RedisCacheable)<br>
4. [Spring Batch JPQL 쿼리 작성하여 조회없이 삭제 + 대규모 데이터 세트에서 배치 단위로 일괄 삭제](https://velog.io/@hyewon0218/%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-%EB%8C%80%EA%B7%9C%EB%AA%A8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%84%B8%ED%8A%B8%EC%97%90%EC%84%9C-%EC%84%B1%EB%8A%A5-%EB%AC%B8%EC%A0%9C%EB%A5%BC-%ED%94%BC%ED%95%98%EA%B8%B0-%EC%9C%84%ED%95%B4-%EB%B0%B0%EC%B9%98-%EB%8B%A8%EC%9C%84%EB%A1%9C-%EC%82%AD%EC%A0%9C)<br>
5. [알람 기능 구현하기(Server Sent Event) vs LongPolling vs Polling 방식과 비교](https://velog.io/@hyewon0218/SSE)<br>
6. [채팅 기능 구현하기(Stomp) vs WebSocket 방식과 비교 + JWT로 사용자 인증](https://velog.io/@hyewon0218/%EC%8B%A4%EC%8B%9C%EA%B0%84-%EC%B1%84%ED%8C%85-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0)<br>
7. [실시간 통신 기술 비교 및 분석 (LongPolling vs SSE / Websocket vs Stomp) K6로 성능 테스트](https://velog.io/@hyewon0218/%EB%B6%80%ED%95%98%ED%85%8C%EC%8A%A4%ED%8A%B8)<br>
8. [Kafka 활용하여 SSE 알람 비동기 처리하기 + 전후 성능 테스트](https://velog.io/@hyewon0218/%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-Kafka-%ED%99%9C%EC%9A%A9%ED%95%98%EC%97%AC-SSE-%EC%95%8C%EB%9E%8C-%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%B2%98%EB%A6%AC%ED%95%98%EA%B8%B0)<br>
9. [프로젝트에 Nginx, Docker, DockerFile, DockerCompose 적용하기](https://velog.io/@hyewon0218/CI-CD-Nginx-Docker-DockerFile-DockerCompose)<br>
10. [Scale-Up, Scale-Out(Nginx로 로드밸런싱)하여 성능 개선 + nGrinder로 성능 테스트](https://velog.io/@hyewon0218/%EC%84%B1%EB%8A%A5-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EC%8A%A4%EC%BC%80%EC%9D%BC%EC%97%85-%EC%8A%A4%EC%BC%80%EC%9D%BC%EC%95%84%EC%9B%83)
11. [Jenkins(젠킨스) CI/CD 구축](https://velog.io/@hyewon0218/CI-CD-Spring-React-Jenkins%EC%A0%A0%ED%82%A8%EC%8A%A4-CICD-%EA%B5%AC%EC%B6%95-with-AWS-EC2)<br>