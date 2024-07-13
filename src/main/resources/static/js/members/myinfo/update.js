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

    // 닉네임 수정 모달 열기
    $('#updateBtn').on('click', function() {
        $.ajax({
            url: "/api/members/myinfo",
            type: "GET",
            contentType: 'application/json',
            success: function(data) {
                console.log(data);
                $('#memberNickname').val(data.memberNickname);
                $('#updateModal').show(); // 모달 열기
            },
            error: function(xhr, status, error) {
                console.error("Error fetching myinfo: " + error);
            }
        });
    });

    // 모달 닫기
    $('.close').on('click', function() {
        $('#updateModal').hide();
    });

    // 닉네임 수정 처리
    $('#updateExecuteBtn').on('click', function() {
        if (!$('#updateForm')[0].checkValidity()) {
            alert("닉네임을 입력해주세요");
            return;
        }
3
        const updateData = {
            memberNickname: $('#memberNickname').val(),
        };

        if (confirm("닉네임을 수정하시겠습니까?")) {
            $.ajax({
                url: "/api/members",
                type: "PUT",
                contentType: "application/json",
                data: JSON.stringify(updateData),
                success: function() {
                    alert("닉네임이 수정되었습니다.");
                    $('#updateModal').hide(); // 모달 닫기
                    loadMyinfo(); // 회원 정보 새로고침
                },
                error: function(xhr) {
                    alert("닉네임을 수정하는 데 실패했습니다.");
                }
            });
        }
    });
});
