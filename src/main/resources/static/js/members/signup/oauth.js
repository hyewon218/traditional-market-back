//$(document).ready(function() {
//    var isNicknameAvailable = false; // 닉네임 중복 확인 여부를 나타내는 변수
//
//    $.ajax({
//        url: "/api/members/myinfo",
//        type: "GET",
//        contentType: 'application/json',
//        success: function(data) {
//            console.log(data);
//            $('#memberNickname').val(data.memberNickname);
//        },
//        error: function(xhr, status, error) {
//            console.error("Error fetching myinfo: " + error);
//        }
//    });
//
//    // 닉네임 중복 확인 버튼 클릭 시 이벤트 처리
//    $('#nicknameCheckButton').click(function () {
//        $.ajax({
//            url: '/api/members/countNickname',
//            type: 'GET',
//            contentType: 'application/json',
//            data: {
//                memberNickname: $('#memberNickname').val()
//            },
//            success: function (result) {
//                console.log(result);
//                $('#nicknameNotAvailable').hide();
//                $('#nicknameAvailable').show().text(result.message).append($('<br />'));
//                isNicknameAvailable = true;
//            },
//            error: function (error) {
//                console.log(error);
//                $('#nicknameAvailable').hide();
//                $('#nicknameNotAvailable').show().text(error.responseJSON.message).append($('<br />'));
//                isNicknameAvailable = false;
//            }
//        });
//    });
//
//    $("#addinfoForm").submit(function(e) {
//        e.preventDefault();
//        var memberNickname = $('#memberNickname').val();
//
//        if(!isNicknameAvailable || !memberNickname) {
//            e.preventDefault();
//            alert("닉네임 중복 확인해주세요");
//        } else {
//            $.ajax({
//                type: 'PUT',
//                contentType: 'application/json',
//                url: '/api/members/addinfo',
//                data: JSON.stringify({
//                    memberNickname: memberNickname
//                }),
//                success: function(response) {
//                    console.log(response);
//                    alert('회원가입이 완료되었습니다.');
//                    window.location.href = '/';
//                },
//                error: function(xhr, status, error) {
//                    var errorMessage = xhr.responseJSON.message;
//                    alert('회원가입에 실패하였습니다' + errorMessage);
//                }
//            });
//        }
//    });
//});

$(document).ready(function() {

    $.ajax({
        url: "/api/members/myinfo",
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);
            $('#memberNickname').val(data.memberNickname);
        },
        error: function(xhr, status, error) {
            console.error("Error fetching myinfo: " + error);
        }
    });

    $("#addinfoForm").submit(function(e) {
        e.preventDefault();
        var memberNickname = $('#memberNickname').val();

        $.ajax({
            type: 'PUT',
            contentType: 'application/json',
            url: '/api/members/addinfo',
            data: JSON.stringify({
                memberNickname: memberNickname
            }),
            success: function(response) {
                console.log(response);
                alert('회원가입이 완료되었습니다.');
                window.location.href = '/';
            },
            error: function(xhr, status, error) {
                var errorMessage = xhr.responseJSON.message;
                alert('회원가입에 실패하였습니다' + errorMessage);
            }
        });
    });
});