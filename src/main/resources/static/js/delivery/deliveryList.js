$(document).ready(function() {
    // 배송지 목록 조회
    function loadDeliveryList() {
        $.ajax({
            url: "/api/deliveries",
            type: "GET",
            success: function(data) {
                console.log(data);
                let deliveryListHtml = '';

                data.content.forEach(function(delivery) {
                    deliveryListHtml += `
                        <div class="delivery-item">
                            <h3>${delivery.receiver} (${delivery.title})</h3>
                            <p>${delivery.phone}</p>
                            <p>${delivery.roadAddr} ${delivery.extraAddr ? `${delivery.extraAddr}` : ''} ${delivery.detailAddr} (${delivery.postCode})</p>
                            <div>
                                <button class="edit-btn" data-delivery-no="${delivery.deliveryNo}">수정</button>
                                <button class="delete-btn" data-delivery-no="${delivery.deliveryNo}">삭제</button>
                                <button class="set-primary-btn" data-delivery-no="${delivery.deliveryNo}" ${delivery.primary ? 'disabled' : ''}>설정</button>
                                <button class="remove-primary-btn" data-delivery-no="${delivery.deliveryNo}" ${delivery.primary ? '' : 'disabled'}>해제</button>
                                <button class="select-btn"
                                                data-title="${delivery.receiver} (${delivery.title})"
                                                data-phone="${delivery.phone}"
                                                data-addr="${delivery.roadAddr} ${delivery.extraAddr ? `${delivery.extraAddr}` : ''} ${delivery.detailAddr} (${delivery.postCode})">
                                                선택</button>
                            </div>
                        </div>
                        <hr/>`; // 구분선 추가
                });

                $(".deliveryList-contents").html(deliveryListHtml);

                // 추가 버튼 클릭 시 추가 페이지 이동
                $("#addDeliveryBtn").on("click", function() {
                    window.location.href = `/delivery/add`; // 추가 페이지로 이동
                });

                // 수정 버튼 클릭 시 수정 페이지 이동
                $(".edit-btn").on("click", function() {
                    const deliveryNo = $(this).data("delivery-no");
                    window.location.href = `/delivery/deliverylist/${deliveryNo}`; // 수정 페이지로 이동
                });

                // 선택 버튼 클릭 시 메인 페이지(test.html)에 선택된 배송지 정보 전달
                $(".select-btn").on("click", function() {
                    const delivery = {
                        title: $(this).data('title'),
                        phone: $(this).data('phone'),
                        address: $(this).data('addr'),
                    };
                    window.opener.postMessage(delivery, '*');
                    console.log("메인 페이지에 전달된 선택된 배송지 정보:", delivery);
                    // 새 창 닫기
                    window.close();
                });
            },
            error: function(xhr) {
                $(".deliveryList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    loadDeliveryList(); // 배송지 목록 로드

    // 배송지 목록 버튼 클릭 시 목록 새로 고침
    $("#viewDeliveryListBtn").on("click", function() {
        // 새 창의 너비와 높이
        const windowWidth = 570;
        const windowHeight = 600;

        // 새 창의 left와 top을 계산하여 중앙으로 위치시킴
        const left = (window.screen.width - windowWidth) / 2;
        const top = (window.screen.height - windowHeight) / 2;

        // 팝업 창을 열기 위해 필요한 스타일 속성
        const popupStyle = `width=${windowWidth}, height=${windowHeight}, left=${left}, top=${top}, resizable=yes, scrollbars=yes, status=yes`;

        // 주소 및 스타일 지정
        window.open('/delivery/deliverylist', '_blank', popupStyle);
        loadDeliveryList();
    });

    // 기본배송지 설정
    $(document).on('click', '.set-primary-btn', function() {
        const deliveryNo = $(this).data('delivery-no');

        if (confirm("기본배송지로 설정하시겠습니까?")) {
            $.ajax({
                url: `/api/deliveries/primary/${deliveryNo}`,
                type: "PUT",
                success: function() {
                    alert("기본배송지로 설정되었습니다.");
                    loadDeliveryList(); // 배송지 목록 새로고침
                },
                error: function(xhr) {
                    alert("기본배송지 설정에 실패했습니다.");
                }
            });
        }
    });

    // 기본배송지 해제
    $(document).on('click', '.remove-primary-btn', function() {
        const deliveryNo = $(this).data('delivery-no');

        if (confirm("기본배송지를 해제하시겠습니까?")) {
            $.ajax({
                url: "/api/deliveries/delprimary",
                type: "PUT",
                success: function() {
                    alert("기본배송지가 해제되었습니다.");
                    loadDeliveryList(); // 배송지 목록 새로고침
                },
                error: function(xhr) {
                    alert("기본배송지 해제에 실패했습니다.");
                }
            });
        }
    });

    // 삭제 버튼 클릭 시 배송지 삭제
    $(document).on('click', '.delete-btn', function() {
        const deliveryNo = $(this).data('delivery-no');

        if (confirm("배송지를 삭제하시겠습니까?")) {
            $.ajax({
                url: `/api/deliveries/${deliveryNo}`,
                type: "DELETE",
                success: function() {
                    alert("배송지가 삭제되었습니다.");
                    loadDeliveryList(); // 배송지 목록 새로고침
                },
                error: function(xhr) {
                    alert("배송지 삭제에 실패했습니다.");
                }
            });
        }
    });
});
