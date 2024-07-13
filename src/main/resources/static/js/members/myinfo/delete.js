$(document).ready(function() {
    var sentVerificationCode; // 전송된 인증번호를 저장할 변수
    var isVerified = false; // 인증번호 확인 여부를 나타내는 변수

    // 내정보 불러와서 memberEmail 채우기
    $.ajax({
        url: "/api/members/myinfo",
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);
            $('#memberEmail').text(data.memberEmail);
        },
        error: function(xhr, status, error) {
            console.error("Error fetching myinfo: " + error);
        }
    });

    // 인증번호 전송 버튼 클릭 시 이벤트 처리
    $("#certifyEmailButton").click(function() {
        var memberEmail = $('#memberEmail').text();

        // 서버로 이메일 주소 전송 및 인증 요청
        $.ajax({
            type: "POST",
            url: "/api/send-mail/email",
            data: JSON.stringify({ memberEmail: memberEmail }),
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function(response) {
                console.log(response);
                sentVerificationCode = response.code; // 서버에서 전송한 인증번호 저장
                $("#certifyEmailMessage").text("이메일로 인증번호가 전송되었습니다.").show();
                isVerified = false; // 인증번호 확인 여부 초기화
            },
            error: function() {
                console.log("memberEmail : " + memberEmail);
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

    // 인증번호 입력란에 변동 생기면 인증번호 확인 여부 초기화됨
    $('#emailVerificationCode').on('input', function() {
        isVerified = false;
        $('#certifyEmailMessage').hide();
    });

    // 폼 제출 시 인증번호 확인 여부 체크 및 회원가입 처리
    $("#verifyForm").submit(function(e) {
        e.preventDefault(); // 기본 제출 동작 방지

       if ($("#emailVerificationCode").val() !== sentVerificationCode || !isVerified) {
            $("#certifyEmailMessage").hide();
            alert("인증번호를 확인해주세요.");
            e.preventDefault(); // 인증번호 확인이 되지 않으면 제출을 막음
       } else if (isVerified) {
            if (confirm("인증이 완료됐습니다. 탈퇴하시겠습니까? 탈퇴 후 복구는 불가합니다.")) {
                $.ajax({
                    url: "/api/members",
                    type: "DELETE",
                    success: function() {
                        alert("정상적으로 탈퇴처리 되었습니다. 이용해주셔서 감사합니다.");
                        window.location.href = '/';
                    },
                    error: function(xhr) {
                        alert("탈퇴처리 실패했습니다.");
                    }
                });
            }
       }
    });
});