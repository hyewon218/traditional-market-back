$(document).ready(function() {
    // URL에서 noticeNo 가져오기
    var url = window.location.href;
    var noticeNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("noticeNo : " + noticeNo);

    $.ajax({
        url: "/api/notices/" + noticeNo,
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);

            $('title').text(data.noticeTitle + " 상세보기");
            $('#h2').text(data.noticeTitle + " 상세정보");

            // 문의사항 정보 설정
            $('#noticeTitle').text(data.noticeTitle);
            $('#noticeWriter').text(data.noticeWriter);
            $('#noticeContent').text(data.noticeContent);
            $('#createTime').text(data.createTime);

            // 이미지 미리보기 추가
            if (data.imageList && data.imageList.length > 0) {
                data.imageList.forEach(function(image) {
                    $('#imageContainer').append(
                        '<img src="' + image.imageUrl + '" alt="공지사항 이미지" class="preview-image" onclick="openModal(this)">'
                    );
                });
            } else {
                $('#imageContainer').append('<p>등록된 이미지가 없습니다.</p>');
            }
        },
        error: function(xhr, status, error) {
            console.error("공지사항 상세보기 가져오는 중 오류: " + error);
        }
    });

    // 삭제 버튼 클릭 이벤트 핸들러
    $(".delete-notice").on("click", function() {
        $('#adminPw').val('');
        $("#checkPwError").text('');
        $('#verifyPwModal').show();

        // 전체 삭제 버튼 클릭 이벤트 핸들러
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
                    // 비밀번호가 일치할 경우 공지사항 삭제 전에 확인 팝업
                    if (confirm("정말로 이 공지사항을 삭제하시겠습니까?")) {
                        $.ajax({
                            url: "/api/notices/" + noticeNo,
                            type: "DELETE",
                            success: function(response) {
                                alert(response.message);
                                $('#verifyPwModal').hide();
                                window.location.href = '/admin/notices';
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

    // 수정 버튼 클릭 이벤트 핸들러
    $(".update-notice").on("click", function() {
        window.location.href = "/admin/notices/" + noticeNo; // 수정 페이지로 이동
    });
});

function openModal(img) {
    var modal = $('#myModal');
    var modalImg = $('#img01');
    var captionText = $('#caption');

    modal.show();
    modalImg.attr('src', img.src);
    captionText.text(img.alt);
}

// 모달 외부 클릭 시 모달 닫기
$('#myModal').on('click', function(event) {
    if (event.target === this) {
        closeModal();
    }
});

function closeModal() {
    $('#myModal').hide();
}

// 비밀번호 확인 모달 닫기
$('.close').on('click', function() {
    $('#verifyPwModal').hide();
});