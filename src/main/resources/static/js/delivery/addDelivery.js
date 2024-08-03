$(document).ready(function() {

    // 배송지 추가 버튼 클릭
    $('#saveDeliveryBtn').on('click', function() {
        if (!$('#addDeliveryForm')[0].checkValidity()) {
            alert("모든 필드를 올바르게 입력하세요.");
            return;
        }

        const deliveryData = {
            title: $('#addTitle').val(),
            receiver: $('#addReceiver').val(),
            phone: $('#addPhone').val(),
            postCode: $('#addPostCode').val(),
            roadAddr: $('#addRoadAddr').val(),
            jibunAddr: $('#addJibunAddr').val(),
            detailAddr: $('#addDetailAddr').val(),
            extraAddr: $('#addExtraAddr').val(),
        };

        $.ajax({
            url: `/api/deliveries`,
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(deliveryData),
            success: function() {
                alert("배송지가 추가되었습니다.");
                window.location.href = `/delivery/deliverylist`;
            },
            error: function(xhr) {
                alert("배송지 추가에 실패했습니다.");
            }
        });
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
});
