$(document).ready(function() {
    // 배송지 목록 로드
    function loadDeliveryList() {
        $.ajax({
            url: "/api/deliveries",
            type: "GET",
            success: function(data) {
                console.log(data);
                let deliveryTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>기본배송지</th>
                                <th>배송지 이름</th>
                                <th>받는 사람</th>
                                <th>휴대전화번호</th>
                                <th>우편번호</th>
                                <th>도로명주소</th>
                                <th>지번주소</th>
                                <th>상세주소</th>
                                <th>참고사항</th>
                                <th>수정</th>
                                <th>삭제</th>
                                <th>기본배송지 설정</th>
                            </tr>
                        </thead>
                        <tbody>`;

                data.forEach(function(delivery) {
                    deliveryTable += `
                        <tr>
                            <td>${delivery.primary ? "기본배송지" : ""}</td>
                            <td>${delivery.title}</td>
                            <td>${delivery.receiver}</td>
                            <td>${delivery.phone}</td>
                            <td>${delivery.postCode}</td>
                            <td>${delivery.roadAddr}</td>
                            <td>${delivery.jibunAddr}</td>
                            <td>${delivery.detailAddr}</td>
                            <td>${delivery.extraAddr}</td>
                            <td><button class="edit-btn" data-delivery-no="${delivery.deliveryNo}">수정</button></td>
                            <td><button class="delete-btn" data-delivery-no="${delivery.deliveryNo}">삭제</button></td>
                            <td>
                                <button class="set-primary-btn" data-delivery-no="${delivery.deliveryNo}" ${delivery.primary ? 'disabled' : ''}>설정</button>
                                <button class="remove-primary-btn" data-delivery-no="${delivery.deliveryNo}" ${delivery.primary ? '' : 'disabled'}>해제</button>
                            </td>
                        </tr>`;
                });

                deliveryTable += `
                        </tbody>
                    </table>`;

                $(".deliveryList-contents").html(deliveryTable);
            },
            error: function(xhr) {
                $(".deliveryList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    loadDeliveryList(); // 배송지 목록 로드

    // 배송지 추가 모달 열기
    $('#addDeliveryBtn').on('click', function() {
        $('#addDeliveryModal').show(); // 배송지 추가 모달 열기
        $('#addDeliveryForm')[0].reset(); // 폼 필드 초기화
    });

    // 수정 버튼 클릭 시 모달 열기
    $(document).on('click', '.edit-btn', function() {
        const deliveryNo = $(this).data('delivery-no');

        $.ajax({
            url: `/api/deliveries/${deliveryNo}`,
            type: "GET",
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

                $('#editDeliveryModal').show(); // 모달 열기
            },
            error: function(xhr) {
                alert("배송지 정보를 가져오는 데 실패했습니다.");
            }
        });
    });

    // 배송지 추가 모달 저장 버튼 클릭
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
                $('#addDeliveryModal').hide(); // 모달 닫기
                loadDeliveryList(); // 배송지 목록 새로고침
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

    // 모달 닫기
    $('.close').on('click', function() {
        $('#editDeliveryModal').hide();
        $('#addDeliveryModal').hide();
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
                $('#editDeliveryModal').hide(); // 모달 닫기
                loadDeliveryList(); // 배송지 목록 새로고침
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

    // 취소 버튼 클릭 시 모달 닫기
    $('#cancelBtn').on('click', function() {
        $('#editDeliveryModal').hide(); // 모달 닫기
    });

    // 배송지 추가 모달 취소 버튼 클릭 시 모달 닫기
    $('#addCancelBtn').on('click', function() {
        $('#addDeliveryModal').hide(); // 모달 닫기
    });
});
