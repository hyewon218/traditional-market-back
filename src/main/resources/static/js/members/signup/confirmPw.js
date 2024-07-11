$(function(){
    $('#memberPw').keyup(function(){
    $('#checkMessage').html('');
    });

    $('#confirmPw').keyup(function(){

        if ($('#memberPw').val() != $('#confirmPw').val()) {
          $('#checkMessage').html('비밀번호 일치하지 않음<br><br>');
          $('#checkMessage').attr('color', 'red');
        } else {
          $('#checkMessage').html('비밀번호 일치함<br><br>');
          $('#checkMessage').attr('color', 'green');
        }
    });
});