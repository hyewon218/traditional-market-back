// Promises와 $.when을 사용하여 각 상품의 상점과 시장 정보를 비동기적으로 가져와 처리한 후, 모든 정보가 준비된 후에 테이블을 한 번에 추가
$(document).ready(function() {
    let currentPage = 0; // 현재 페이지 초기화
    const groupSize = 5; // 한 그룹에 보여줄 페이지 수
    let selectedCategory = ""; // 선택된 카테고리 초기화
    let sortBy = "itemName,desc"; // 기본 정렬 기준

    loadMarkets();
    loadAllItems(currentPage, selectedCategory);

    // 카테고리 목록
    const categories = ["과일", "채소", "육류", "생선"];

    // 카테고리 버튼 생성
    categories.forEach(category => {
        $(".itemList-category").append(`<button class="category-btn" data-item-category="${category}">${category}</button>`);
    });

    // 조회수순 버튼 클릭 이벤트
    $("#viewCountButton").click(function() {
        sortBy = "viewCount,desc"; // 조회수 내림차순으로 정렬 기준 설정
        const selectedMarketNo = $('#marketNo').val();
        const selectedShopNo = $('#shopNo').val();

        if (selectedMarketNo === 'all') {
            loadAllItems(currentPage, selectedCategory);
        } else if (selectedMarketNo) {
            loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
        }
    });

    // 시장 목록을 가져와서 드롭다운 채우기
    function loadMarkets() {
        $.ajax({
            url: '/api/markets',
            type: 'GET',
            contentType: 'application/json',
            success: function(data) {
                const marketSelect = $('#marketNo');
                marketSelect.empty(); // 기존 옵션 제거
                marketSelect.append('<option value="all">전체보기</option>'); // 전체보기 옵션 추가

                data.content.forEach(market => {
                    marketSelect.append(`
                        <option value="${market.marketNo}">${market.marketName}</option>
                    `);
                });

                // 드롭다운 변경 이벤트 핸들러 추가
                marketSelect.on('change', function() {
                    const selectedMarketNo = $(this).val();
                    selectedCategory = "";
                    $("#shopNo").html('<option value="all">상점 선택</option>'); // 상점 목록 초기화 및 기본값 설정
                    sortBy = "itemName,desc";
                    if (selectedMarketNo === 'all') {
                        loadAllItems(currentPage, selectedCategory);
                    } else if (selectedMarketNo) {
                        loadItemList(selectedMarketNo, currentPage, selectedCategory);
                        loadShopList(selectedMarketNo); // 상점 목록 로드
                    } else {
                        $(".shopList-contents").html('<h2>상품 관리</h2><p>시장 선택이 필요합니다.</p>');
                    }
                });
            },
            error: function(xhr, status, error) {
                console.error("시장 목록 불러오기 오류: " + error);
            }
        });
    }

    // 특정 시장에 해당하는 상점 목록 드롭다운 채우기
    function loadShopList(marketNo) {
        $.ajax({
            url: `/api/${marketNo}/shops`,
            type: 'GET',
            contentType: 'application/json',
            success: function(data) {
                const shopSelect = $('#shopNo');
                shopSelect.empty(); // 기존 옵션 제거
                shopSelect.append('<option value="all">전체보기</option>'); // 전체보기 옵션 추가

                data.content.forEach(shop => {
                    shopSelect.append(`
                        <option value="${shop.shopNo}">${shop.shopName}</option>
                    `);
                });

                // 드롭다운 변경 이벤트 핸들러 추가
                shopSelect.on('change', function() {
                    const selectedMarketNo = $('#marketNo').val();
                    const selectedShopNo = $(this).val();
                    selectedCategory = "";
                    sortBy = "itemName,desc";
                    loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
                });
            },
            error: function(xhr, status, error) {
                console.error("상점 목록 불러오기 오류: " + error);
            }
        });
    }

    // 특정 시장에 해당하는 상품 목록을 가져오는 함수
    function loadItemList(marketNo, shopNo, page, itemCategory) {
        let url = `/api/${marketNo}/items?page=${page}&size=3&sort=${sortBy}`;

        if (itemCategory) {
            url = `/api/${marketNo}/items/category/paging?page=${page}&size=3&sort=${sortBy}&itemCategory=${itemCategory}`;
        } else if(shopNo === "all") {
            url =  `/api/${marketNo}/items?page=${page}&size=3&sort=${sortBy}`;
        } else if(marketNo && shopNo) {
            url = `/api/${shopNo}/items?page=${page}&size=3&sort=${sortBy}`;
        } else {
            url = `/api/market/${marketNo}/items?page=${page}&size=3&sort=${sortBy}`;
        }

        $.ajax({
            url: url,
            type: "GET",
            success: function(data) {
                let contentHtml = `<h2>상품 관리</h2>`;

                if (data.content.length === 0) {
                    $(".itemList-contents").html(`
                        <h2>상품 관리</h2>
                        <p>상품이 존재하지않습니다</p>
                        <button id="viewAllItemsBtn">전체 상품 목록 보기</button>
                    `);
                    $("#viewAllItemsBtn").click(function() {
                        location.reload();
                    });
                    return;
                }

                let itemTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>상품명</th>
                                <th>소속 시장</th>
                                <th>소속 상점</th>
                                <th>가격</th>
                                <th>재고</th>
                                <th>판매상태</th>
                                <th>조회수</th>
                                <th>좋아요 수</th>
                                <th>수정</th>
                                <th>삭제</th>
                            </tr>
                        </thead>
                        <tbody>`;

                let promises = data.content.map(function(item) {
                    let itemUrl = "/admin/items/" + item.itemNo;

                    return $.ajax({
                        url: "/api/shops/" + item.shopNo,
                        type: "GET"
                    }).then(function(shop) {
                        return $.ajax({
                            url: "/api/markets/" + shop.marketNo,
                            type: "GET"
                        }).then(function(market) {
                            let marketUrl = "/admin/markets/" + market.marketNo;
                            let shopUrl = "/admin/shops/" + shop.shopNo;
                            itemTable += `
                                <tr>
                                    <td><a href="${itemUrl}" class="item-title">${item.itemName}</a></td>
                                    <td><a href="${marketUrl}" class="market-title">${market.marketName}</a></td>
                                    <td><a href="${shopUrl}" class="shop-title">${shop.shopName}</a></td>
                                    <td>${item.price}</td>
                                    <td>${item.stockNumber}</td>
                                    <td>${item.itemSellStatus}</td>
                                    <td>${item.viewCount}</td>
                                    <td>${item.likes}</td>
                                    <td>
                                        <button class="update-item" data-item-no="${item.itemNo}">
                                            <a href="/admin/items/u/${item.itemNo}">수정</a>
                                        </button>
                                    </td>
                                    <td>
                                        <button class="delete-item" data-item-no="${item.itemNo}">삭제</button>
                                    </td>
                                </tr>`;
                        });
                    });
                });

                $.when.apply($, promises).then(function() {
                    itemTable += `
                        </tbody>
                    </table>`;

                    contentHtml += itemTable;
                    $(".itemList-contents").html(contentHtml);

                    $(".delete-item").on("click", function() {
                        const itemNo = $(this).data("item-no");
                        $('#adminPw').val('');
                        $("#checkPwError").text('');
                        $('#verifyPwModal').show();

                        $('#deleteExecuteBtn').off('click').on('click', function() {
                            var adminPw = $('#adminPw').val();

                            if (!$('#verifyPwForm')[0].checkValidity()) {
                                alert("비밀번호를 입력해주세요");
                                return;
                            }

                            $.ajax({
                                url: "/api/members/myinfo/check",
                                type: "POST",
                                data: { password: adminPw },
                                success: function(data) {
                                    if (confirm("정말 이 상품을 삭제하시겠습니까?")) {
                                        $.ajax({
                                            url: "/api/items/" + itemNo,
                                            type: "DELETE",
                                            success: function(response) {
                                                alert(response.message);
                                                $('#verifyPwModal').hide();
                                                loadItemList(currentPage);
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

                    renderPagination(data.totalPages);
                });
            },
            error: function(xhr) {
                $(".itemList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    // 전체 상품 목록을 가져오는 함수
    function loadAllItems(page, itemCategory) {
        let url = `/api/items?page=${page}&size=3&sort=${sortBy}`;
        if (itemCategory) {
            url = `/api/items/category?page=${page}&size=3&sort=${sortBy}&itemCategory=${itemCategory}`;
        }

        $.ajax({
            url: url,
            type: "GET",
            success: function(data) {
                let contentHtml = `<h2>상품 관리</h2>`;

                if (data.content.length === 0) {
                    $(".itemList-contents").html(`
                        <h2>상품 관리</h2>
                        <p>선택한 카테고리에 해당하는 상품이 존재하지 않습니다</p>
                        <button id="viewAllItemsBtn">전체 상품 목록 보기</button>
                    `);

                    $("#viewAllItemsBtn").click(function() {
                        location.reload();
                    });
                    return;
                }

                let itemTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>상품명</th>
                                <th>소속 시장</th>
                                <th>소속 상점</th>
                                <th>가격</th>
                                <th>재고</th>
                                <th>판매상태</th>
                                <th>조회수</th>
                                <th>좋아요 수</th>
                                <th>수정</th>
                                <th>삭제</th>
                            </tr>
                        </thead>
                        <tbody>`;

                let promises = data.content.map(function(item) {
                    let itemUrl = "/admin/items/" + item.itemNo;

                    return $.ajax({
                        url: "/api/shops/" + item.shopNo,
                        type: "GET"
                    }).then(function(shop) {
                        return $.ajax({
                            url: "/api/markets/" + shop.marketNo,
                            type: "GET"
                        }).then(function(market) {
                            let marketUrl = "/admin/markets/" + market.marketNo;
                            let shopUrl = "/admin/shops/" + shop.shopNo;
                            itemTable += `
                                <tr>
                                    <td><a href="${itemUrl}" class="item-title">${item.itemName}</a></td>
                                    <td><a href="${marketUrl}" class="market-title">${market.marketName}</a></td>
                                    <td><a href="${shopUrl}" class="shop-title">${shop.shopName}</a></td>
                                    <td>${item.price}</td>
                                    <td>${item.stockNumber}</td>
                                    <td>${item.itemSellStatus}</td>
                                    <td>${item.viewCount}</td>
                                    <td>${item.likes}</td>
                                    <td>
                                        <button class="update-item" data-item-no="${item.itemNo}">
                                            <a href="/admin/items/u/${item.itemNo}">수정</a>
                                        </button>
                                    </td>
                                    <td>
                                        <button class="delete-item" data-item-no="${item.itemNo}">삭제</button>
                                    </td>
                                </tr>`;
                        });
                    });
                });

                $.when.apply($, promises).then(function() {
                    itemTable += `
                        </tbody>
                    </table>`;

                    contentHtml += itemTable;
                    $(".itemList-contents").html(contentHtml);

                    $(".delete-item").on("click", function() {
                        const itemNo = $(this).data("item-no");
                        const selectedMarketNo = $('#marketNo').val();
                        const selectedShopNo = $('#shopNo').val();
                        $('#adminPw').val('');
                        $("#checkPwError").text('');
                        $('#verifyPwModal').show();

                        $('#deleteExecuteBtn').off('click').on('click', function() {
                            var adminPw = $('#adminPw').val();

                            if (!$('#verifyPwForm')[0].checkValidity()) {
                                alert("비밀번호를 입력해주세요");
                                return;
                            }

                            $.ajax({
                                url: "/api/members/myinfo/check",
                                type: "POST",
                                data: { password: adminPw },
                                success: function(data) {
                                    if (confirm("정말 이 상품을 삭제하시겠습니까?")) {
                                        $.ajax({
                                            url: "/api/items/" + itemNo,
                                            type: "DELETE",
                                            success: function(response) {
                                                alert(response.message);
                                                $('#verifyPwModal').hide();
                                                loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
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

                    renderPagination(data.totalPages);
                });
            },
            error: function(xhr) {
                $(".itemList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    // 페이지네이션을 생성하는 함수
    function renderPagination(totalPages) {
        const paginationContainer = $(".pagination");
        paginationContainer.empty();

        const currentGroup = Math.floor(currentPage / groupSize);

        paginationContainer.append('<button class="first-group">처음</button>');

        if (currentGroup > 0) {
            paginationContainer.append('<button class="prev-group">이전</button>');
        }

        const startPage = currentGroup * groupSize;
        const endPage = Math.min(startPage + groupSize - 1, totalPages - 1);

        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            const buttonClass = isActive ? 'active' : '';
            paginationContainer.append(`
                <button class="page-btn ${buttonClass}" data-page="${i}">${i + 1}</button>
            `);
        }

        if (endPage < totalPages - 1) {
            paginationContainer.append('<button class="next-group">다음</button>');
        }

        paginationContainer.append('<button class="last-group">끝</button>');

        $(".page-btn").click(function() {
            const newPage = $(this).data("page");
            currentPage = newPage;
            $(".page-btn").removeClass('active');
            $(this).addClass('active');
            const selectedMarketNo = $('#marketNo').val();
            const selectedShopNo = $('#shopNo').val();
            if (selectedMarketNo === 'all') {
                loadAllItems(newPage, selectedCategory);
            } else if (selectedMarketNo) {
                loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
            }
        });

        $(".next-group").click(function() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                $(".page-btn").removeClass('active');
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                const selectedMarketNo = $('#marketNo').val();
                const selectedShopNo = $('#shopNo').val();
                if (selectedMarketNo === 'all') {
                    loadAllItems(currentPage, selectedCategory);
                } else if (selectedMarketNo) {
                    loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
                }
            }
        });

        $(".prev-group").click(function() {
            if (currentPage > 0) {
                currentPage--;
                $(".page-btn").removeClass('active');
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                const selectedMarketNo = $('#marketNo').val();
                const selectedShopNo = $('#shopNo').val();
                if (selectedMarketNo === 'all') {
                    loadAllItems(currentPage, selectedCategory);
                } else if (selectedMarketNo) {
                    loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
                }
            }
        });

        $(".first-group").click(function() {
            currentPage = 0;
            $(".page-btn").removeClass('active');
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            const selectedMarketNo = $('#marketNo').val();
            const selectedShopNo = $('#shopNo').val();
            if (selectedMarketNo === 'all') {
                loadAllItems(currentPage, selectedCategory);
            } else if (selectedMarketNo) {
                loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
            }
        });

        $(".last-group").click(function() {
            currentPage = totalPages - 1;
            $(".page-btn").removeClass('active');
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            const selectedMarketNo = $('#marketNo').val();
            const selectedShopNo = $('#shopNo').val();
            if (selectedMarketNo === 'all') {
                loadAllItems(currentPage, selectedCategory);
            } else if (selectedMarketNo) {
                loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
            }
        });
    }

    // 카테고리 버튼 클릭 이벤트 핸들러
    $(".itemList-category").on("click", ".category-btn", function() {
        selectedCategory = $(this).data("item-category");
        currentPage = 0; // 페이지 번호 초기화
        const selectedMarketNo = $('#marketNo').val();
        const selectedShopNo = $('#shopNo').val();
        if (selectedMarketNo === 'all') {
            loadAllItems(currentPage, selectedCategory);
        } else if (selectedMarketNo) {
            loadItemList(selectedMarketNo, selectedShopNo, currentPage, selectedCategory);
        }
    });

    // 모달 닫기
    $('.close').on('click', function() {
        $('#verifyPwModal').hide();
    });
});
