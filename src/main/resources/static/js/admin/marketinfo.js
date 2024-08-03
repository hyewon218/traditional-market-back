$(document).ready(function() {
    // URL에서 marketNo 가져오기
    var url = window.location.href;
    var marketNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("marketNo : " + marketNo);

    // 페이지 로드 시 특정 시장 조회
    $.ajax({
        url: "/api/markets/" + marketNo,
        type: "GET",
        success: function(data) {
            console.log(data);
            var marketName = data.marketName;
            var marketAddr = data.marketAddr;

            $('title').text(marketName + " 상세보기(관리자)");
            $('#h2').text(marketName + " 상세정보(관리자)");
            $('#h3').text(marketName + " 날씨정보");

            // 시장 기본 정보 채우기
            $('#marketNo').text(data.marketNo || "정보가 없습니다.");
            $('#marketName').text(data.marketName || "정보가 없습니다.");
            $('#marketAddr').text(data.marketAddr || "정보가 없습니다.");
            $('#marketDetail').text(data.marketDetail || "정보가 없습니다.");
            $('#category').text(data.category || "정보가 없습니다.");
            $('#viewCount').text(data.viewCount || "정보가 없습니다.");
            $('#likes').text(data.likes || "정보가 없습니다.");
            $('#parkingInfo1').text(data.parkingInfo1 || "정보가 없습니다.");
            $('#parkingInfo2').text(data.parkingInfo2 || "정보가 없습니다.");
            $('#busInfo').text(data.busInfo || "정보가 없습니다.");
            $('#busLat').text(data.busLat || "정보가 없습니다.");
            $('#busLng').text(data.busLng || "정보가 없습니다.");
            $('#subwayInfo').text(data.subwayInfo || "정보가 없습니다.");
            $('#subwayLat').text(data.subwayLat || "정보가 없습니다.");
            $('#subwayLng').text(data.subwayLng || "정보가 없습니다.");

            // 이미지 출력
            if (data.imageList && data.imageList.length > 0) {
                data.imageList.forEach(function(image) {
                    $('#imageContainer').append(
                        `<img src="${image.imageUrl}" alt="시장 이미지" class="preview-image" onclick="openModal(this)">`
                    );
                });
            } else {
                $('#imageContainer').append('<p>등록된 이미지가 없습니다.</p>');
            }

            // 상점 목록 출력
            if (data.shopList && data.shopList.length > 0) {
                var shopListHtml = '<ul>';
                data.shopList.forEach(function(shop) {
                    // 상점 이름과 이미지를 포함한 HTML 구성
                    shopListHtml += '<li>';
                    // 상점 이름을 클릭하면 해당 상점으로 이동하는 링크 추가
                    shopListHtml += '<a href="/admin/shops/' + shop.shopNo + '">' + '<strong>' + shop.shopName + '</strong></a><br>';

                    // 상점 이미지 출력
                    if (shop.imageList && shop.imageList.length > 0) {
                        shop.imageList.forEach(function(image) {
                            shopListHtml += '<img src="' + image.imageUrl + '" alt="상점 이미지" style="max-width: 100px;">&nbsp;';
                        });
                    }
                    shopListHtml += '</li>';
                });
                shopListHtml += '</ul>';
                $('#shopContainer').html(shopListHtml);
            } else {
                $('#shopContainer').html('<p>등록된 상점이 없습니다.</p>');
            }

            // 댓글 목록 출력
            if (data.commentList && data.commentList.length > 0) {
                var commentListHtml = '<ul>';
                data.commentList.forEach(function(comment) {
                    commentListHtml += `<li>${comment.comment}</li>`;
                });
                commentListHtml += '</ul>';
                $('#commentContainer').html(commentListHtml);
            } else {
                $('#commentContainer').html('<p>등록된 댓글이 없습니다.</p>');
            }

            // 시장 생성일 및 정보 변경일 출력
            $('#createTime').text(data.createTime);
            $('#updateTime').text(data.updateTime);

            // 수정 버튼 클릭 시 시장 수정 페이지로 이동
            $('#updateButton').on('click', function() {
                window.location.href = "/admin/markets/u/" + marketNo;
            });

            // 삭제 버튼 클릭 시 시장 삭제 처리
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
                            // 비밀번호가 일치할 경우 시장 삭제 전에 확인 팝업
                            if (confirm("정말 이 시장을 삭제하시겠습니까?")) {
                                $.ajax({
                                    url: "/api/markets/" + marketNo,
                                    type: "DELETE",
                                    success: function(response) {
                                        alert(response.message);
                                        $('#verifyPwModal').hide();
                                        window.location.href = "/admin/markets"; // 삭제 후 목록 페이지로 이동
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

            // 시장 주소를 이용해 해당 시장의 주소를 검색하여 지도에 표시하고, 날씨 정보 가져오기
            showMarketInfo(data.marketAddr, marketName);

            // 주차 정보 버튼 클릭 시 모달 표시
            $('#parkingInfoBtn').click(function() {

                var address1 = data.parkingInfo1;
                var address2 = data.parkingInfo2;

                var addresses = [];
                if (address1) addresses.push({ address: address1, type: '주차' });
                if (address2) addresses.push({ address: address2, type: '주차' });

                if (addresses.length > 0) {
                    addresses.push({ address: marketAddr, type: '시장', info: marketName });
                    initializeMap('parkingMap', addresses, '주차장', '주차', true);
                    $('#parkingModal').show();
                } else {
                    alert('주차 정보 주소가 없습니다.');
                }
            });

            // 근처 대중교통 버튼 클릭 시 모달 표시
            $('#nearTransport').click(function() {

                var busLat = data.busLat;
                var busLng = data.busLng;
                var subwayLat = data.subwayLat;
                var subwayLng = data.subwayLng;

                var locations = [];
                if (busLat && busLng) {
                    locations.push({ latitude: busLat, longitude: busLng, type: '버스', info: data.busInfo });
                }
                if (subwayLat && subwayLng) {
                    locations.push({ latitude: subwayLat, longitude: subwayLng, type: '지하철', info: data.subwayInfo });
                }

                if (locations.length > 0) {
                    locations.push({ address: marketAddr, type: '시장', info: marketName });
                    initializeMap('transportMap', locations, '근처 대중교통', null, true);
                    $('#transportModal').show();
                } else {
                    alert('대중교통 정보가 없습니다.');
                }
            });
        },
        error: function(xhr) {
            console.error("시장 상세정보 조회 실패 : " + xhr.responseText);
        }
    });

    // '상점 추가' 버튼 클릭 시 처리
    $('#addShopButton').click(function() {
        const marketNo = $('#marketNo').text().trim();
        const marketName = $('#marketName').text().trim();
        const url = `/admin/shops/a?marketNo=${encodeURIComponent(marketNo)}&marketName=${encodeURIComponent(marketName)}`;
        window.location.href = url;
    });
});

// 네이버 지도 초기화 함수
function initializeMap(containerId, locations, title, type, showInfoWindow) {
    var mapOptions = {
        zoom: 15
    };

    var map = new naver.maps.Map(containerId, mapOptions);
    var markers = [];

    // 주소와 위도, 경도가 포함된 배열 처리
    if (Array.isArray(locations)) {
        locations.forEach(function(location) {
            if (location.address) {
                // 주소를 이용해 위치를 찾는 경우
                naver.maps.Service.geocode({ address: location.address }, function(status, response) {
                    console.log('Geocode response:', response); // 추가된 로그
                    if (status !== naver.maps.Service.Status.OK) {
                        console.error('Geocode error:', status); // 추가된 로그
                        return alert('지도를 표시하는 중 오류가 발생했습니다.');
                    }

                    var result = response.v2;
                    var items = result.addresses;

                    if (items.length > 0) {
                        var firstItem = items[0];
                        var x = parseFloat(firstItem.x);
                        var y = parseFloat(firstItem.y);
                        var jibunAddress = firstItem.jibunAddress;
                        var roadAddress = firstItem.roadAddress;

                        var marker = new naver.maps.Marker({
                            position: new naver.maps.LatLng(y, x),
                            map: map
                        });

                        // 인포윈도우 내용 설정
                        var infoWindowContent;
                        if (location.type === '주차') {
                            infoWindowContent = [
                                '<div style="padding:10px;min-width:200px;line-height:150%;">',
                                '<h3>' + title + '</h3>',
                                '<h4>지번 주소</h4>',
                                '<p>' + jibunAddress + '</p>',
                                '<h4>도로명 주소</h4>',
                                '<p>' + roadAddress + '</p>',
                                '</div>'
                            ].join('');
                        } else if (location.type === '시장') {
                            infoWindowContent = [
                                '<div style="padding:10px;min-width:200px;line-height:150%;">',
                                '<h3>' + location.info + '</h3>',
                                '<h4>지번 주소</h4>',
                                '<p>' + jibunAddress + '</p>',
                                '<h4>도로명 주소</h4>',
                                '<p>' + roadAddress + '</p>',
                                '</div>'
                            ].join('');
                        } else {
                            infoWindowContent = [
                                '<div style="padding:10px;min-width:200px;line-height:150%;">',
                                '<h3>' + title + '</h3>',
                                '<p>정보가 없습니다.</p>',
                                '</div>'
                            ].join('');
                        }

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

                        // 지도 로드 시 정보창 열린 상태로 로드
                        if (showInfoWindow) {
                            infoWindow.open(map, marker);
                        }

                        markers.push(marker);

                        if (markers.length === locations.length) {
                            var bounds = new naver.maps.LatLngBounds();
                            markers.forEach(function(marker) {
                                bounds.extend(marker.getPosition());
                            });
                            map.fitBounds(bounds);
                        }
                    } else {
                        console.log('주소에 대한 검색 결과가 없습니다');
                    }
                });
            } else if (location.latitude && location.longitude) {
                // 위도와 경도를 이용해 위치를 표시하는 경우
                var marker = new naver.maps.Marker({
                    position: new naver.maps.LatLng(location.latitude, location.longitude),
                    map: map
                });

                // 인포윈도우 내용 설정
                var infoWindowContent;
                if (location.type === '버스' || location.type === '지하철') {
                    infoWindowContent = [
                        '<div style="padding:10px;min-width:200px;line-height:150%;">',
                        '<h3>' + title + '</h3>',
                        '<h4>' + '가까운 ' + location.type + ' 정보' + '</h4>',
                        '<p>' + location.info + '</p>',
                        '</div>'
                    ].join('');
                } else if (location.type === '시장') {
                    infoWindowContent = [
                        '<div style="padding:10px;min-width:200px;line-height:150%;">',
                        '<h3>' + location.info + '</h3>',
                        '<h4>지번 주소</h4>',
                        '<p>' + location.address + '</p>',
                        '</div>'
                    ].join('');
                } else {
                    infoWindowContent = [
                        '<div style="padding:10px;min-width:200px;line-height:150%;">',
                        '<h3>' + title + '</h3>',
                        '<p>정보가 없습니다.</p>',
                        '</div>'
                    ].join('');
                }

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
                if (showInfoWindow) {
                    infoWindow.open(map, marker);
                }

                markers.push(marker);

                if (markers.length === locations.length) {
                    var bounds = new naver.maps.LatLngBounds();
                    markers.forEach(function(marker) {
                        bounds.extend(marker.getPosition());
                    });
                    map.fitBounds(bounds);
                }
            }
        });
    } else {
        // 단일 주소 처리
        var address = locations.address;
        if (address) {
            naver.maps.Service.geocode({ address: address }, function(status, response) {
                console.log('Geocode response:', response); // 추가된 로그
                if (status !== naver.maps.Service.Status.OK) {
                    return alert('지도를 표시하는 중 오류가 발생했습니다.');
                }

                var result = response.v2;
                var items = result.addresses;

                if (items.length > 0) {
                    var firstItem = items[0];
                    var x = parseFloat(firstItem.x);
                    var y = parseFloat(firstItem.y);
                    var jibunAddress = firstItem.jibunAddress;
                    var roadAddress = firstItem.roadAddress;

                    var marker = new naver.maps.Marker({
                        position: new naver.maps.LatLng(y, x),
                        map: map
                    });

                    var infoWindowContent;
                    if (type === '주차') {
                        infoWindowContent = [
                            '<div style="padding:10px;min-width:200px;line-height:150%;">',
                            '<h3>' + title + '</h3>',
                            '<h4>지번 주소</h4>',
                            '<p>' + jibunAddress + '</p>',
                            '<h4>도로명 주소</h4>',
                            '<p>' + roadAddress + '</p>',
                            '</div>'
                        ].join('');
                    } else if (type === '시장') {
                        infoWindowContent = [
                            '<div style="padding:10px;min-width:200px;line-height:150%;">',
                            '<h3>' + title + '</h3>',
                            '<h4>지번 주소</h4>',
                            '<p>' + jibunAddress + '</p>',
                            '<h4>도로명 주소</h4>',
                            '<p>' + roadAddress + '</p>',
                            '</div>'
                        ].join('');
                    } else {
                        infoWindowContent = [
                            '<div style="padding:10px;min-width:200px;line-height:150%;">',
                            '<h3>' + title + '</h3>',
                            '<p>정보가 없습니다.</p>',
                            '</div>'
                        ].join('');
                    }

                    var infoWindow = new naver.maps.InfoWindow({
                        content: infoWindowContent
                    });

                    // 마커 클릭 시 정보창 열기
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
                    if (showInfoWindow) {
                        infoWindow.open(map, marker);
                    }

                    map.setCenter(new naver.maps.LatLng(y, x));
                } else {
                    console.log('주소에 대한 검색 결과가 없습니다');
                }
            });
        } else if (locations.latitude && locations.longitude) {
            var marker = new naver.maps.Marker({
                position: new naver.maps.LatLng(locations.latitude, locations.longitude),
                map: map
            });

            var infoWindowContent;
            if (type === '버스' || type === '지하철') {
                infoWindowContent = [
                    '<div style="padding:10px;min-width:200px;line-height:150%;">',
                    '<h3>' + title + '</h3>',
                    '<h4>' + '가까운 ' + type + ' 정보' + '</h4>',
                    '<p>' + locations.info + '</p>',
                    '</div>'
                ].join('');
            } else if (type === '시장') {
                infoWindowContent = [
                    '<div style="padding:10px;min-width:200px;line-height:150%;">',
                    '<h3>' + title + '</h3>',
                    '<h4>지번 주소</h4>',
                    '<p>' + locations.address + '</p>',
                    '</div>'
                ].join('');
            } else {
                infoWindowContent = [
                    '<div style="padding:10px;min-width:200px;line-height:150%;">',
                    '<h3>' + title + '</h3>',
                    '<p>정보가 없습니다.</p>',
                    '</div>'
                ].join('');
            }

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
            if (showInfoWindow) {
                infoWindow.open(map, marker);
            }

            map.setCenter(new naver.maps.LatLng(locations.latitude, locations.longitude));
        }
    }
}

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

$('#parkingModal').on('click', function(event) {
    if (event.target === this) {
        closeParkingModal();
    }
});

$('#transportModal').on('click', function(event) {
    if (event.target === this) {
        closeTransportModal();
    }
});

// 비밀번호 확인 모달 닫기
$('.close').on('click', function() {
    $('#verifyPwModal').hide();
});

function closeModal() {
    $('#myModal').hide();
}

// 주차 정보 모달 닫기
function closeParkingModal() {
    $('#parkingModal').hide();
}

// 대중교통 모달 닫기
function closeTransportModal() {
    $('#transportModal').hide();
}

