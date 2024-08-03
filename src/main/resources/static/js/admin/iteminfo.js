$(document).ready(function() {
    // URL에서 itemNo 가져오기
    var url = window.location.href;
    var itemNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("itemNo : " + itemNo);

    // 상품 정보 불러오기
    $.ajax({
        url: "/api/items/" + itemNo,
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);
            var itemName = data.itemName;
            var itemNo = data.itemNo;
            var shopNo = data.shopNo;

            $('title').text(itemName + " 상세보기");
            $('#h2').text(itemName + " 상세정보");

            // 이미지 출력
            if (data.imageList && data.imageList.length > 0) {
                data.imageList.forEach(function(image) {
                    $('#imageContainer').append(
                        `<img src="${image.imageUrl}" alt="상품 이미지" class="preview-image" onclick="openModal(this)">`
                    );
                });
            } else {
                $('#imageContainer').append('<p>등록된 이미지가 없습니다.</p>');
            }

            // 상품 정보 설정
            $('#itemNo').text(data.itemNo);
            $('#itemName').text(data.itemName);
            $('#price').text(data.price);
            $('#stockNumber').text(data.stockNumber);
            $('#itemDetail').text(data.itemDetail);
            $('#itemCategory').text(data.itemCategory);

            // 판매상태 한글로 변환
            let itemSellStatusName;
            switch (data.itemSellStatus) {
                case 'SELL': itemSellStatusName = '판매중'; break;
                case 'SOLD_OUT': itemSellStatusName = '품절'; break;
            }
            $('#itemSellStatus').text(itemSellStatusName);
            $('#viewCount').text(data.viewCount);
            $('#likes').text(data.likes);

            // 댓글 목록 출력
            if (data.shopCommentList && data.shopCommentList.length > 0) {
                var commentListHtml = '<ul>';
                data.shopCommentList.forEach(function(comment) {
                    commentListHtml += `<li>${comment.comment}</li>`;
                });
                commentListHtml += '</ul>';
                $('#commentContainer').html(commentListHtml);
            } else {
                $('#commentContainer').html('<p>등록된 댓글이 없습니다.</p>');
            }

            // 상품 생성일 및 정보 변경일 출력
            $('#createTime').text(data.createTime);
            $('#updateTime').text(data.updateTime);

            // 수정 버튼 클릭 시 상품 수정 페이지로 이동
            $('#updateButton').on('click', function() {
                window.location.href = "/admin/items/u/" + itemNo;
            });

            // 삭제 버튼 클릭 시 상품 삭제 처리
            $('#deleteButton').on('click', function() {
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
                            // 비밀번호가 일치할 경우 전체 삭제 전에 확인 팝업
                            if (confirm("정말 이 상품을 삭제하시겠습니까?")) {
                                $.ajax({
                                    url: "/api/items/" + itemNo,
                                    type: "DELETE",
                                    success: function(response) {
                                        alert(response.message);
                                        $('#verifyPwModal').hide();
                                        window.location.href = "/admin/items"; // 삭제 후 목록 페이지로 이동
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

            // 상점 정보 조회, 이후 상점 이름을 '소속 상점'(shopName) 필드에 매핑
            $.ajax({
                url: "/api/shops/" + shopNo,
                type: "GET",
                contentType: 'application/json',
                success: function(shopData) {
                    console.log(shopData);
                    $('#shopName').text(shopData.shopName);

                    // marketNo을 이용하여 시장 정보 조회, 이후 시장 이름을 '소속 시장'(marketName) 필드에 매핑
                    var marketNo = shopData.marketNo;
                    $.ajax({
                        url: "/api/markets/" + marketNo,
                        type: "GET",
                        contentType: 'application/json',
                        success: function(marketData) {
                            console.log(marketData);
                            $('#marketName').text(marketData.marketName);
                        },
                        error: function(xhr) {
                            console.error("시장 정보 조회 실패 : " + xhr.responseText);
                        }
                    });
                },
                error: function(xhr) {
                    console.error("상점 정보 조회 실패 : " + xhr.responseText);
                }
            });
        },
        error: function(xhr) {
            console.error("상품 상세정보 조회 실패 : " + xhr.responseText);
        }
    });
});

// 모달 관련 함수
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

