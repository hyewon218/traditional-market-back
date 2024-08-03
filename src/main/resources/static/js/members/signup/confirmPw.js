// 새로운 비밀번호 입력 시 일치 여부 확인 함수
$(function() {
    function checkPasswordMatch() {
        // 두 필드가 모두 비어있을 경우 메시지 초기화
        if ($('#memberPw').val() === '' && $('#confirmPw').val() === '') {
            $('#checkMessage').html('');
            return;
        }

        if ($('#memberPw').val() !== $('#confirmPw').val()) {
            $('#checkMessage').html('비밀번호 일치하지 않음<br><br>').css('color', 'red');
        } else {
            $('#checkMessage').html('비밀번호 일치함<br><br>').css('color', 'green');
        }
    }

    $('#memberPw, #confirmPw').keyup(checkPasswordMatch);
});
