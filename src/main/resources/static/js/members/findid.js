$(document).ready(function() {
    var sentVerificationCode; // 전송된 인증번호를 저장할 변수
    var isVerified = false; // 인증번호 확인 여부를 나타내는 변수

    // 인증번호 전송 버튼 클릭 시 이벤트 처리
    $("#certifyEmailButton").click(function() {
        var memberEmail = $('#memberEmail').val();
        if (!memberEmail) {
          alert("이메일을 입력해주세요.");
          return;
        }

        $("#certifyEmailMessage").text("인증번호 전송중...").show();

        // 서버로 이메일 주소 전송 및 인증 요청
        $.ajax({
            type: "POST",
            url: "/api/send-mail/email/findid",
            data: JSON.stringify({ memberEmail: memberEmail }),
            contentType: "application/json; charset=utf-8",
//            dataType: "json",
            success: function(response) {
                console.log(response);
                sentVerificationCode = response.code; // 서버에서 전송한 인증번호 저장
                $("#certifyEmailMessage").text("이메일로 인증번호가 전송되었습니다.").show();
                isVerified = false; // 인증번호 확인 여부 초기화
            },
            error: function() {
                $("#certifyEmailMessage").text("이메일 인증번호 전송에 실패했습니다.").show();
            }
        });
    });

    // 인증번호 확인 버튼 클릭 시 이벤트 처리
    $("#verifyEmailButton").click(function() {
        var enteredCode = $("#emailVerificationCode").val();
        if (enteredCode === "") {
            alert("인증번호를 입력해주세요."); // 인증번호란이 비어있을 때 알림 표시
            return;
        }

        if (enteredCode === sentVerificationCode) {
            $("#certifyEmailMessage").text("인증번호 일치").show();
            isVerified = true; // 인증번호 확인됨
        } else {
            $("#certifyEmailMessage").text("인증번호 불일치").show();
            isVerified = false; // 인증번호 확인 여부 초기화
        }
    });

    // 아이디 반환 모달 열기
    $('#findIdButton').on('click', function() {
        var memberEmail = $('#memberEmail').val();
        var emailVerificationCode = $('#emailVerificationCode').val();

        // 입력 검증
        if (!memberEmail) {
            alert("이메일을 입력해주세요.");
            return;
        }
        if (!emailVerificationCode) {
            alert("인증번호를 입력해주세요.");
            return;
        }
        if (!isVerified) {
            alert("인증번호를 확인해주세요.");
            return;
        }

        // 서버에 아이디 요청
         $.ajax({
             type: "POST",
             url: "/api/members/findid",
             data: JSON.stringify({
                memberEmail: memberEmail,
                code: emailVerificationCode
                }),
             contentType: "application/json",
             success: function(response) {
                 console.log(response);
                 $("#foundIdMessage").text(response).show();
                 $("#idModal").show();
             },
             error: function() {
                 alert("아이디 찾기 실패");
             }
         });
    });

    // 모달 닫기
    $('.close').on('click', function() {
        $('#idModal').hide();
    });
});
