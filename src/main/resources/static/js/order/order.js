// 배송지, 배송메시지 함께 저장
$(document).ready(function() {
    // 페이지 로드 시 기본 배송지 조회
    $.ajax({
        url: "/api/deliveries/primary",
        type: "GET",
        success: function(data) {
            console.log(data);
            // 성공 시 선택된 배송지를 출력
            const deliveryTitle = `${data.receiver} (${data.title})`;
            const deliveryPhone = `${data.phone}`;
            const deliveryAddr = `${data.roadAddr} ${data.extraAddr ? data.extraAddr : ''} ${data.detailAddr} (${data.postCode})`;
            $("#selectedDeliveryTitle").text(deliveryTitle);
            $("#selectedDeliveryPhone").text(deliveryPhone);
            $("#selectedDeliveryAddr").text(deliveryAddr);
        },
        error: function(xhr) {
            // 실패 시 에러 메시지 출력
            console.error("배송지 조회 실패:", xhr.responseText);
            $("#selectedDeliveryAddr").text("배송지 정보를 가져오지 못했습니다. 오른쪽 상단의 배송지 목록 버튼을 클릭해 배송지를 선택해주세요");
        }
    });

    // 페이지 로드 시 주문 정보 조회
    $.ajax({
        url: '/api/orders',
        type: 'GET',
        success: function(response) {
            console.log("response : ", response);
            const order = response.content[0];
            console.log("order : ", order);

            if (order && order.orderItemList && order.orderItemList.length > 0) {
                const orderItems = order.orderItemList[0];
                console.log("orderItems : ", orderItems);

                // orderItems가 존재하고 imageList도 존재하는지 확인
                if (orderItems && orderItems.imageList && orderItems.imageList.length > 0) {
                    const imageList = orderItems.imageList[0];
                    console.log("imageList : ", imageList);

                    if (imageList && imageList.imageUrl) {
                        const totalPrice = orderItems.orderPrice * orderItems.count;

                        $('#orderDetails').html(`
                            <img src="${imageList.imageUrl}" alt="${orderItems.itemName}" width="150" height="150" />
                            <p>상품명: ${orderItems.itemName}</p>
                            <p>상품 수량: ${orderItems.count}</p>
                            <p>상품 가격: ${totalPrice} 원</p>
                        `);
                    } else {
                        $('#orderDetails').html('<p>주문 상품 정보를 가져오는 데 실패했습니다.</p>');
                    }
                } else {
                    $('#orderDetails').html('<p>주문 상품 이미지 정보가 없습니다.</p>');
                }
            } else {
                $('#orderDetails').html('<p>주문 정보가 없습니다.</p>');
            }
        },
        error: function(error) {
            $('#orderDetails').html('<p>주문 정보를 가져오는 데 실패했습니다.</p>');
        }
    });

    // 새창(delivery.deliveryList)에서 전달된 선택된 배송지 정보 처리
    $(window).on('message', function(event) {
        console.log("event.originalEvent.data : " + event.originalEvent.data)
        const selectedDelivery = event.originalEvent.data;
        if (selectedDelivery) {
            // 선택된 배송지 정보를 화면에 업데이트
            $("#selectedDeliveryTitle").text(selectedDelivery.title);
            $("#selectedDeliveryPhone").text(selectedDelivery.phone);
            $("#selectedDeliveryAddr").text(selectedDelivery.address);
            $("#orderInfo").removeClass("hidden");
            console.log("새창에서 전달된 선택된 배송지 정보:", selectedDelivery);
        }
    });

    // 페이지 새로고침 시 이전 선택된 배송지 정보 확인
    window.onbeforeunload = function() {
        return "이전 내용을 불러오시겠습니까?";
    };

    // 결제 버튼 클릭 시 카카오페이 결제 요청
    $("#payButton").click(function() {
        // 비활성화
        window.onbeforeunload = null;

        // 현재 선택된 배송지, 배송메시지 확인
        const selectedDeliveryAddr = $("#selectedDeliveryAddr").text().trim(); // trim : 문자열 양 끝에 있는 공백을 제거
        const savedDeliveryMessage = $("#customMessageTextarea").val(); // val()로 <textarea> 요소의 실제 입력 값 가져오기
        console.log(selectedDeliveryAddr);
        console.log(savedDeliveryMessage);

        // 배송지가 선택되어 있는지 확인
        if (selectedDeliveryAddr && selectedDeliveryAddr !== "배송지 정보를 가져오지 못했습니다. 오른쪽 상단의 배송지 목록 버튼을 클릭해 배송지를 선택해주세요") {
            // 결제 요청
            requestPayment(selectedDeliveryAddr, savedDeliveryMessage);
        } else {
            // 배송지가 선택되지 않았을 경우 경고 메시지 출력
            alert("배송지를 입력해주세요");
        }
    });

    // 카카오페이 결제 요청
    function requestPayment(selectedDeliveryAddr, savedDeliveryMessage) {
        $.ajax({
            url: "/api/payment/ready",
            type: "POST",
            success: function(response) {
                // 결제 요청 성공 시 선택된 배송지를 서버에 저장
                saveSelectedDelivery(selectedDeliveryAddr, savedDeliveryMessage);
                // 카카오 결제 요청 URL로 리다이렉트
                console.log(response);
                window.location.href = response.next_redirect_pc_url;
            },
            error: function(error) {
                alert("주문 정보를 가져오는 데 실패했습니다.");
            }
        });
    }

    // 선택된 배송지를 서버에 저장하는 함수
    function saveSelectedDelivery(selectedDeliveryAddr, savedDeliveryMessage) {
        $.ajax({
            url: "/api/orders/delivery",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify({
                delivery: selectedDeliveryAddr,
                deliveryMessage: savedDeliveryMessage
            }),
            success: function(response) {
                console.log("배송지 저장 성공:", response);
            },
            error: function(xhr) {
                console.error("배송지 저장 실패:", xhr.responseText);
            }
        });
    }
});