$(document).ready(function() {

    // 임시비밀번호 발급 버튼 클릭 시 이벤트
    $('#temppasswordButton').on('click', function() {
        var memberId = $('#memberId').val();
        var memberEmail = $('#memberEmail').val();

        // 입력 검증
        if(!memberId) {
            alert("아이디를 입력해주세요.");
            return;
        }
        if (!memberEmail) {
            alert("이메일을 입력해주세요.");
            return;
        }

        $("#certifyEmailMessage").text("임시비밀번호 발급중...").show();

         // 서버에 아이디, 이메일에 해당하는 회원인지 확인하고 맞으면 임시비밀번호 발급
         $.ajax({
             type: "POST",
             url: "/api/send-mail/email/temppw",
             data: JSON.stringify({
                memberId : memberId,
                memberEmail: memberEmail
                }),
             contentType: "application/json; charset=utf-8",
             success: function(response) {
                 console.log(response);
                 $("#certifyEmailMessage").text("이메일로 임시비밀번호가 발급되었습니다.").show();
             },
             error: function(xhr) {
                 console.error("Error details:", xhr.responseText);
                 console.error("Status code:", xhr.status);
                 $("#certifyEmailMessage").text("임시비밀번호 발급에 실패했습니다.").show();
             }
         });
    });
});
