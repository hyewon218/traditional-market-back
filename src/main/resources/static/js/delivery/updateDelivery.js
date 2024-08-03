$(document).ready(function() {
    // 해당 배송지 불러오기
    // URL에서 deliveryNo를 가져오기
    var url = window.location.href;
    var deliveryNo = url.substring(url.lastIndexOf('/') + 1);
    console.log("deliveryNo : " + deliveryNo);

    $.ajax({
        url: `/api/deliveries/${deliveryNo}`,
        type: "GET",
        contentType: 'application/json',
        success: function(delivery) {
            $('#deliveryNo').val(deliveryNo);
            $('#title').val(delivery.title);
            $('#receiver').val(delivery.receiver);
            $('#phone').val(delivery.phone);
            $('#postCode').val(delivery.postCode);
            $('#roadAddr').val(delivery.roadAddr);
            $('#jibunAddr').val(delivery.jibunAddr);
            $('#detailAddr').val(delivery.detailAddr);
            $('#extraAddr').val(delivery.extraAddr);
        },
        error: function(xhr) {
            alert("배송지 정보를 가져오는 데 실패했습니다.");
        }
    });

    // 주소 검색 함수
    function sample4_execDaumPostcode(mode) {
        new daum.Postcode({
            oncomplete: function(data) {
                var roadAddr = data.roadAddress;
                var extraRoadAddr = '';

                if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
                    extraRoadAddr += data.bname;
                }
                if (data.buildingName !== '' && data.apartment === 'Y') {
                    extraRoadAddr += (extraRoadAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                if (extraRoadAddr !== '') {
                    extraRoadAddr = ' (' + extraRoadAddr + ')';
                }

                if (mode === 'add') {
                    document.getElementById('addPostCode').value = data.zonecode;
                    document.getElementById("addRoadAddr").value = roadAddr;
                    document.getElementById("addJibunAddr").value = data.jibunAddress;
                    document.getElementById("addExtraAddr").value = extraRoadAddr;
                } else {
                    document.getElementById('postCode').value = data.zonecode;
                    document.getElementById("roadAddr").value = roadAddr;
                    document.getElementById("jibunAddr").value = data.jibunAddress;
                    document.getElementById("extraAddr").value = extraRoadAddr;
                }
            }
        }).open();
    }

    // 주소 검색 버튼 클릭
    $("input[type='button'][value='주소 검색']").on('click', function() {
        const mode = $(this).closest('form').is('#addDeliveryForm') ? 'add' : 'edit';
        sample4_execDaumPostcode(mode);
    });


    // 배송지 수정 처리
    $('#updateDeliveryBtn').on('click', function() {
        if (!$('#editDeliveryForm')[0].checkValidity()) {
            alert("모든 필드를 올바르게 입력하세요.");
            return;
        }

        const deliveryNo = $('#deliveryNo').val();
        const deliveryData = {
            title: $('#title').val(),
            receiver: $('#receiver').val(),
            phone: $('#phone').val(),
            postCode: $('#postCode').val(),
            roadAddr: $('#roadAddr').val(),
            jibunAddr: $('#jibunAddr').val(),
            detailAddr: $('#detailAddr').val(),
            extraAddr: $('#extraAddr').val(),
        };

        $.ajax({
            url: `/api/deliveries/${deliveryNo}`,
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify(deliveryData),
            success: function() {
                alert("배송지 정보가 수정되었습니다.");
                window.location.href = "/delivery/deliverylist";
            },
            error: function(xhr) {
                alert("배송지 정보를 수정하는 데 실패했습니다.");
            }
        });
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
                url: `/api/deliveries/delprimary/${deliveryNo}`,
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

});
