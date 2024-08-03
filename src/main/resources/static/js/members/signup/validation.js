$(document).ready(function() {
    var sentVerificationCode; // 전송된 인증번호를 저장할 변수
    var isVerified = false; // 인증번호 확인 여부를 나타내는 변수
    var isEmailAvailable = false; // 이메일 중복 확인 여부를 나타내는 변수
    var isIdAvailable = false; // 아이디 중복 확인 여부를 나타내는 변수

    // 이메일 중복 확인 버튼 클릭 시 이벤트 처리
    $('#emailCheckButton').click(function () {
        $.ajax({
            url: '/api/members/checkemail',
            type: 'GET',
            contentType: 'application/json',
            data: {
                memberEmail: $('#memberEmail').val()
            },
            success: function (result) {
                console.log(result);
                $('#emailNotAvailable').hide();
                $('#emailAvailable').show().text(result.message).append($('<br />'));
                isEmailAvailable = true;
            },
            error: function (error) {
                console.log(error);
                $('#emailAvailable').hide();
                $('#emailNotAvailable').show().text(error.responseJSON.message).append($('<br />'));
                isEmailAvailable = false;
            }
        });
    });

    // 아이디 중복 확인 버튼 클릭 시 이벤트 처리
    $('#idCheckButton').click(function () {
        $.ajax({
            url: '/api/members/checkid',
            type: 'GET',
            contentType: 'application/json',
            data: {
                memberId: $('#memberId').val()
            },
            success: function (result) {
                console.log(result);
                $('#idNotAvailable').hide();
                $('#idAvailable').show().text(result.message).append($('<br />'));
                isIdAvailable = true;
            },
            error: function (error) {
                console.log(error);
                $('#idAvailable').hide();
                $('#idNotAvailable').show().text(error.responseJSON.message).append($('<br />'));
                isIdAvailable = false;
            }
        });
    });

    // 인증번호 전송 버튼 클릭 시 이벤트 처리
    $("#certifyEmailButton").click(function() {
        var memberEmail = $('#memberEmail').val();

        // 이메일 중복 확인 버튼을 눌렀는지 확인
        if (!isEmailAvailable) {
            alert("이메일 중복 확인을 먼저 해주세요.");
            return;
        }

        $("#certifyEmailMessage").text("인증번호 전송중...").show();

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

    // 이메일 입력란에 변동 생기면 이메일 중복 확인 여부 초기화됨
    $('#memberEmail').on('input', function() {
        isEmailAvailable = false;
        isVerified = false;
        $('#emailAvailable').hide();
        $('#emailNotAvailable').hide();
        $('#certifyEmailMessage').hide();
    });

    // 인증번호 입력란에 변동 생기면 인증번호 확인 여부 초기화됨
    $('#emailVerificationCode').on('input', function() {
        isVerified = false;
        $('#certifyEmailMessage').hide();
    });

    // 아이디 입력란에 변동 생기면 아이디 중복 확인 여부 초기화됨
    $('#memberId').on('input', function() {
        isIdAvailable = false;
        $('#idAvailable').hide();
        $('#idNotAvailable').hide();
    });

    // select 엘리먼트 변경 시 이메일 중복 확인 및 인증번호 확인 여부 초기화
    $('#domainList').on('change', function() {
        isEmailAvailable = false;
        isVerified = false;
        $('#emailAvailable').hide();
        $('#emailNotAvailable').hide();
        $('#certifyEmailMessage').hide();
    });

    // 폼 제출 시 인증번호 확인 여부 체크 및 회원가입 처리
    $("#signupForm").submit(function(e) {
        e.preventDefault(); // 기본 제출 동작 방지

        var memberPw = $('#memberPw').val();
        var confirmPw = $('#confirmPw').val();

        if (memberPw !== confirmPw) {
            alert("비밀번호와 비밀번호 확인이 일치하지 않습니다");
            e.preventDefault();
        } else if ($("#emailVerificationCode").val() !== sentVerificationCode || !isVerified) {
            $("#certifyEmailMessage").hide();
            alert("인증번호를 확인해주세요.");
            e.preventDefault(); // 인증번호 확인이 되지 않으면 제출을 막음
        } else if (!isEmailAvailable) {
            e.preventDefault(); // 이메일 중복 확인이 되지 않으면 제출을 막음
            alert("이메일 중복 확인해주세요.");
        } else if (!isIdAvailable) {
            e.preventDefault(); // 아이디 중복 확인이 되지 않으면 제출을 막음
            alert("아이디 중복 확인해주세요");
        } else {
            var formData = {
                memberId: $('#memberId').val(),
                memberPw: memberPw,
                memberEmail: $('#memberEmail').val(),
                memberNickname: $('#memberNickname').val()
            };

            $.ajax({
                type: 'POST',
                contentType: 'application/json',
                url: '/api/members/signup',
                data: JSON.stringify(formData),
                success: function(response) {
                    console.log(response);
                    alert('회원가입이 완료되었습니다.');
                    window.location.href = '/members/login';
                },
                error: function(xhr, status, error) {
                    var errorMessage = xhr.responseJSON.message;
                    alert('회원가입에 실패하였습니다');
                }
            });
        }
    });
});