// 기존 주소값 이용해 상점 위치 출력하던 방식에서 위도, 경도값으로 상점 위치 출력하는 방식으로 변경
$(document).ready(function() {
    var marketNo, marketName;

    // URL에서 shopNo를 가져오기
    var url = window.location.href;
    var shopNo = url.substring(url.lastIndexOf('/') + 1);
    var shopName;
    console.log("shopNo : " + shopNo);

    // 특정 상점 조회
    $.ajax({
        url: "/api/shops/" + shopNo,
        type: "GET",
        contentType: 'application/json',
        success: function(data) {
            console.log(data);
            shopName = data.shopName;
            marketNo = data.marketNo;

            $('title').text(shopName + " 상세보기");
            $('#h2').text(shopName + " 상세정보");
            $('#h3').text(shopName + " 날씨정보");

            // 이미지 출력
            if (data.imageList && data.imageList.length > 0) {
                data.imageList.forEach(function(image) {
                    $('#imageContainer').append(
                        `<img src="${image.imageUrl}" alt="상점 이미지" class="preview-image" onclick="openModal(this)">`
                    );
                });
            } else {
                $('#imageContainer').append('<p>등록된 이미지가 없습니다.</p>');
            }

            // 상점 정보 설정
            $('#shopNo').text(data.shopNo);
            $('#shopName').text(data.shopName);
            $('#tel').text(data.tel);
            $('#shopAddr').text(data.shopAddr);
            $('#sellerName').text(data.sellerName);

            // 카테고리 한글로 변환
            var categoryName;
            switch (data.category) {
                case 'AGRI': categoryName = '농산물'; break;
                case 'MARINE': categoryName = '수산물'; break;
                case 'LIVESTOCK': categoryName = '축산물'; break;
                case 'FRUITS': categoryName = '과일'; break;
                case 'PROCESSED': categoryName = '가공식품'; break;
                case 'RICE': categoryName = '쌀'; break;
                case 'RESTAURANT': categoryName = '식당'; break;
                case 'SIDEDISH': categoryName = '반찬'; break;
                case 'STUFF': categoryName = '잡화'; break;
                case 'ETC': categoryName = '기타'; break;
            }
            $('#category').text(categoryName);
            $('#likes').text(data.likes);

            // 상품 목록 출력
            if (data.itemList && data.itemList.length > 0) {
                var itemListHtml = '<ul>';
                data.itemList.forEach(function(item) {
                    itemListHtml += '<li>';
                    itemListHtml += `<a href="/items/${item.itemNo}"><strong>${item.itemName}</strong></a><br>`;

                    if (item.imageList && item.imageList.length > 0) {
                        item.imageList.forEach(function(image) {
                            itemListHtml += `<img src="${image.imageUrl}" alt="상품 이미지" style="max-width: 100px;">&nbsp;`;
                        });
                    }
                    itemListHtml += '</li>';
                });
                itemListHtml += '</ul>';
                $('#itemContainer').html(itemListHtml);
            } else {
                $('#itemContainer').html('<p>등록된 상품이 없습니다.</p>');
            }

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

            // 수정 버튼 클릭 시 상점 수정 페이지로 이동
            $('#updateButton').on('click', function() {
                window.location.href = "/admin/shops/u/" + shopNo;
            });

            // 삭제 버튼 클릭 시 상점 삭제 처리
            $('#deleteButton').on('click', function() {
                $('#adminPw').val('');
                $("#checkPwError").text('');
                $('#verifyPwModal').show();

                // 전체 삭제 버튼 클릭 이벤트 핸들러
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
                            if (confirm("정말 이 상점을 삭제하시겠습니까?")) {
                                $.ajax({
                                    url: "/api/shops/" + shopNo,
                                    type: "DELETE",
                                    success: function(response) {
                                        alert(response.message);
                                        $('#verifyPwModal').hide();
                                        window.location.href = "/admin/shops"; // 삭제 후 목록 페이지로 이동
                                    },
                                    error: function(xhr) {
                                        alert('삭제 실패: ' + xhr.responseText);
                                    }
                                });
                            }
                        },
                        error: function(xhr) {
                            const errorResponse = JSON.parse(xhr.responseText);
                            const errorMsg = errorResponse.message;
                            $("#checkPwError").text(errorMsg);
                        }
                    });
                });
            });

            // 지도에 상점 위치 표시
            if (data.shopLat && data.shopLng) {
                initializeMap('map', [{
                    latitude: data.shopLat,
                    longitude: data.shopLng,
                    type: '상점',
                    info: shopName,
                    tel: data.tel
                }], shopName);
            } else {
                $('#map').html('<p>위도와 경도 정보가 없습니다.</p>');
            }

            // 시장 정보 조회, 이후 시장 이름을 '소속 시장' 필드에 매핑
            $.ajax({
                url: "/api/markets/" + marketNo,
                type: "GET",
                contentType: 'application/json',
                success: function(marketData) {
                    console.log(marketData);
                    marketName = marketData.marketName;
                    $('#marketName').text(marketName);
                },
                error: function(xhr) {
                    console.error("시장 정보 조회 실패 : " + xhr.responseText);
                }
            });
        },
        error: function(xhr) {
            console.error("상점 상세정보 조회 실패 : " + xhr.responseText);
        }
    });

    // '상품 추가' 버튼 클릭 시 처리
    $('#addItemButton').click(function() {
        if(marketNo && marketName && shopNo && shopName) {
            const url = `/admin/items/a?marketNo=${encodeURIComponent(marketNo)}&marketName=${encodeURIComponent(marketName)}&shopNo=${encodeURIComponent(shopNo)}&shopName=${encodeURIComponent(shopName)}`;
            window.location.href = url;
        } else {
            console.log("시장, 상점 정보를 불러오지못했습니다");
        }
    });
});

// 지도 초기화 함수
function initializeMap(containerId, locations, title) {
    var mapOptions = {
        zoom: 7
    };

    var map = new naver.maps.Map(containerId, mapOptions);
    var markers = [];

    // 위치 데이터 처리
    locations.forEach(function(location) {
        var marker = new naver.maps.Marker({
            position: new naver.maps.LatLng(location.latitude, location.longitude),
            map: map
        });

        var infoWindowContent = [
            '<div style="padding:10px;min-width:200px;line-height:150%;">',
            '<h3>' + location.info + '</h3>',
            '<h3>' + '전화번호 : ' + location.tel + '</h3>',
            '</div>'
        ].join('');

        var infoWindow = new naver.maps.InfoWindow({
            content: infoWindowContent
        });

        naver.maps.Event.addListener(marker, "click", function() {
            if (infoWindow.getMap()) {
                infoWindow.close();
            } else {
                infoWindow.open(map, marker);
            }
        });

        // 마커 외 클릭 시 정보창 닫기
        naver.maps.Event.addListener(map, 'click', function() {
            infoWindow.close();
        });

        // 자동으로 정보창 열기
        infoWindow.open(map, marker);

        markers.push(marker);

        if (markers.length === locations.length) {
            var bounds = new naver.maps.LatLngBounds();
            markers.forEach(function(marker) {
                bounds.extend(marker.getPosition());
            });
            map.fitBounds(bounds);
        }
    });
}

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

// 모달 닫기
$('.close').on('click', function() {
    $('#verifyPwModal').hide();
});
