//$(document).ready(function() {
//    // URL에서 memberNo 가져오기
//    var url = window.location.href;
//    var memberNo = url.substring(url.lastIndexOf('/') + 1);
//    console.log("memberNo : " + memberNo);
//
//    $.ajax({
//        url: "/api/members/myinfo/" + memberNo,
//        type: "GET",
//        contentType: 'application/json',
//        success: function(data) {
//            console.log(data);
//            $('#memberId').text(data.memberId);
//            $('#memberEmail').text(data.memberEmail);
//            $('#nicknameWithRandomTag').text(data.nicknameWithRandomTag);
//            $('#role').val(data.role);
//            $('#createTime').text(data.createTime);
//
//            if (data.role === 'ADMIN') {
//                alert("현재 권한이 ADMIN 입니다. 다른 권한으로 수정이 불가능합니다.");
//                $('#role').prop('disabled', true);
//                $('#updateRoleForm button[type="submit"]').prop('disabled', true);
//                $('#deleteButton').prop('disabled', true);
//            } else {
//                $('#myinfo').hide();
//            }
//        },
//        error: function(xhr, status, error) {
//            console.error("회원 상세정보 조회 실패: " + error);
//        }
//    });
//
//    // 권한 수정 버튼 이벤트
//    $('#updateRoleForm').on('submit', function(event) {
//        event.preventDefault();
//
//        let newRole = $("#role").val();
//
//        let data = {
//            role: newRole
//        };
//
//        if(confirm("회원 권한을 수정하시겠습니까?")) {
//            $.ajax({
//                url: "/api/members/admin/u/" + memberNo,
//                type: "PUT",
//                contentType: "application/json",
//                data: JSON.stringify(data),
//                success: function (response) {
//                    alert("권한이 성공적으로 수정되었습니다.");
//                    window.location.href = "/admin/members";
//                },
//                error: function (xhr, status, error) {
//                    alert("권한 수정에 실패했습니다.");
//                }
//            });
//        }
//    });
//
//    // 삭제 버튼 클릭 시 회원 삭제 처리
//    $('#deleteButton').on('click', function() {
//        if (confirm("정말 이 회원을 삭제하시겠습니까?")) {
//            $.ajax({
//                url: "/api/members/admin/r/" + memberNo,
//                type: "DELETE",
//                success: function(response) {
//                    alert("회원이 성공적으로 삭제되었습니다");
//                    window.location.href = "/admin/members";
//                },
//                error: function(xhr) {
//                    alert('삭제 실패: ' + xhr.responseText);
//                }
//            });
//        }
//    });
//});

// 삭제 시 관리자 비밀번호 확인 모달창 테스트
$(document).ready(function() {
    // URL에서 memberNo 가져오기
    var url = window.location.href;
    var memberNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("memberNo : " + memberNo);

    $.ajax({
        url: "/api/members/myinfo/" + memberNo,
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);
            $('#memberId').text(data.memberId);
            $('#memberEmail').text(data.memberEmail);
            $('#nicknameWithRandomTag').text(data.nicknameWithRandomTag);
            $('#role').val(data.role);
            $('#createTime').text(data.createTime);

            if (data.role === 'ADMIN') {
                alert("현재 권한이 ADMIN 입니다. 다른 권한으로 수정이 불가능합니다.");
                $('#role').prop('disabled', true);
                $('#updateRoleForm button[type="submit"]').prop('disabled', true);
                $('#deleteButton').prop('disabled', true);
            } else {
                $('#myinfo').hide();
            }
        },
        error: function(xhr, status, error) {
            console.error("회원 상세정보 조회 실패: " + error);
        }
    });

    // 권한 수정 버튼 이벤트
    $('#updateRoleForm').on('submit', function(event) {
        event.preventDefault();

        let newRole = $("#role").val();

        let data = {
            role: newRole
        };

        if(confirm("회원 권한을 수정하시겠습니까?")) {
            $.ajax({
                url: "/api/members/admin/u/" + memberNo,
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify(data),
                success: function (response) {
                    alert("권한이 성공적으로 수정되었습니다.");
                    window.location.href = "/admin/members";
                },
                error: function (xhr, status, error) {
                    alert("권한 수정에 실패했습니다.");
                }
            });
        }
    });

    // 삭제 버튼 클릭 시 회원 삭제 처리
    $('#deleteButton').on('click', function() {
        $('#adminPw').val('');
        $("#checkPwError").text('');
        $('#verifyPwModal').show();

        // 기존의 클릭 이벤트 핸들러 제거
        $('#deleteExecuteBtn').off('click').on('click', function() {
            var adminPw = $('#adminPw').val();

            if (!$('#verifyPwForm')[0].checkValidity()) {
                alert("비밀번호를 입력해주세요");
                return;
            }

            // 비밀번호 일치 확인
            $.ajax({
                url: "/api/members/myinfo/check",
                type: "POST",
                data: { password: adminPw },
                success: function(data) {
                    console.log(data);
                    // 비밀번호가 일치할 경우 회원 삭제 전에 확인 팝업
                    if (confirm("정말 이 회원을 삭제하시겠습니까?")) {
                        $.ajax({
                            url: "/api/members/admin/r/" + memberNo,
                            type: "DELETE",
                            success: function(response) {
                                alert("회원이 성공적으로 삭제되었습니다");
                                $('#verifyPwModal').hide();
                                window.location.href = "/admin/members";
                            },
                            error: function(xhr) {
                                alert('삭제 실패: ' + xhr.responseText);
                            }
                        });
                    }
                },
                error: function(xhr) {
                    // 비밀번호 확인 실패 시 오류 메시지 표시
                    const errorResponse = JSON.parse(xhr.responseText);
                    const errorMsg = errorResponse.message;
                    $("#checkPwError").text(errorMsg);
                }
            });
        });
    });

    // 모달 닫기
    $('.close').on('click', function() {
        $('#verifyPwModal').hide();
    });
});

