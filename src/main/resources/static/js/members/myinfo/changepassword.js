$(document).ready(function() {
    function loadMyinfo() {
        $.ajax({
            url: "/api/members/myinfo",
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                $('#memberId').text(data.memberId);
                $('#memberEmail').text(data.memberEmail);
                $('#nicknameWithRandomTag').text(data.nicknameWithRandomTag);
                $('#role').text(data.role);
                $('#createTime').text(data.createTime);
            },
            error: function(xhr, status, error) {
                console.error("Error fetching myinfo: " + error);
            }
        });
    }
    loadMyinfo();

    // 현재 비밀번호 확인 모달 열기
    $('#changePwBtn').on('click', function() {
        $('#currentPw').val('') // 입력창 초기화
        $('#checkPwError').text('') // 오류메시지창 초기화
        $('#checkPwModal').show(); // 모달 열기
    });

    // 모달 닫기
    $('.close').on('click', function() {
        $('#checkPwModal').hide();
        $('#changePwModal').hide();
    });

    // 현재 비밀번호 확인 처리
    $('#checkPwExecuteBtn').on('click', function() {
        if (!$('#checkPwForm')[0].checkValidity()) {
            alert("현재 비밀번호를 입력해주세요");
            return;
        }

        const currentPw = $("#currentPw").val();

        $.ajax({
            url: "/api/members/myinfo/check",
            type: "POST",
            data: { password: currentPw },
            success: function(data) {
                alert("비밀번호 일치");
                $('#checkPwModal').hide();
                $('#newPw').val('');
                $('#confirmPw').val('');
                $('#changePwError').text(''); // 오류메시지창 초기화
                $('#changePwModal').show();
            },
            error: function(xhr) {
                // 비밀번호 확인 실패 시 오류 메시지 표시
                const errorResponse = JSON.parse(xhr.responseText);
                const errorMsg = errorResponse.message;
                $("#checkPwError").text(errorMsg);
            }
        });
    });

    // 새로운 비밀번호 입력 시 일치 여부 확인 함수
    function checkPasswordMatch() {
        // 두 필드가 모두 비어있을 경우 메시지 초기화
        if ($('#newPw').val() === '' && $('#confirmPw').val() === '') {
            $('#checkMessage').html('');
            return;
        }

        if ($('#newPw').val() !== $('#confirmPw').val()) {
            $('#checkMessage').html('비밀번호 일치하지 않음<br><br>').css('color', 'red');
        } else {
            $('#checkMessage').html('비밀번호 일치함<br><br>').css('color', 'green');
        }
    }
    $('#newPw, #confirmPw').keyup(checkPasswordMatch);

    // 비밀번호 변경 처리
    $('#changePwExecuteBtn').on('click', function() {
        const newPw = $("#newPw").val();
        const confirmPw = $("#confirmPw").val();

        // 빈칸 있는지 확인
        if (!$('#changePwForm')[0].checkValidity()) {
            alert("변경할 비밀번호를 입력해주세요");
            return;
        }

        // 비밀번호 일치 여부 확인
        if (newPw !== confirmPw) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        if (confirm("비밀번호를 변경하시겠습니까?")) {
            $.ajax({
                url: "/api/members/changepw",
                type: "PUT",
                contentType: "application/json", // JSON 형식으로 데이터 전송
                data: JSON.stringify({
                    changePw: newPw,
                    confirmPw: confirmPw
                }),
                success: function(data) {
                    console.log(data);
                    alert("비밀번호가 변경되었습니다.");
                    $('#changePwModal').hide();
                    loadMyinfo();
                },
                error: function(xhr) {
                    alert("비밀번호 변경 실패");
                    const errorResponse = JSON.parse(xhr.responseText);
                    const errorMsg = errorResponse.message;
                    console.error("Error details:", errorMsg);
                }
            });
        }
    });
});
