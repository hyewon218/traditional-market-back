$(document).ready(function() {
    let currentPage = 0; // 현재 페이지 초기화
    const groupSize = 5; // 한 그룹에 보여줄 페이지 수
    let searchQuery = ""; // 검색어 초기화
    let selectedCategory = ""; // 선택된 카테고리 초기화
    let sortBy = "marketName,asc"; // 기본 정렬 기준

    loadMarketList(currentPage, searchQuery, selectedCategory);

    // 초기 카테고리 버튼 생성
    const categories = ["서울", "인천", "경기도", "강원", "충청도", "경상도", "전라도", "제주도"];
    categories.forEach(category => {
        $(".marketList-category").append(`<button class="category-btn" data-category="${category}">${category}</button>`);
    });

    // 전체보기 버튼 클릭 이벤트
    $("#viewAllButton").click(function() {
        selectedCategory = ""; // 카테고리 선택 초기화
        searchQuery = ""; // 검색어 초기화
        $("#searchInput").val('');
        currentPage = 0; // 페이지 초기화
        sortBy = "marketName,asc"; // 정렬 기준 초기화
        loadMarketList(currentPage, searchQuery, selectedCategory);
    });

    // 조회수순 버튼 클릭 이벤트
    $("#viewCountButton").click(function() {
        sortBy = "viewCount,desc"; // 조회수 내림차순으로 정렬 기준 설정
        loadMarketList(currentPage, searchQuery, selectedCategory);
    });

    // 시장 목록을 가져오는 함수
    function loadMarketList(page, keyword, category) {
        let url;
        const params = new URLSearchParams();

        // 검색어가 있을 경우
        if (keyword) {
            url = "/api/markets/search"
            params.append("keyword", keyword);

        } else if(category) {
            url = "/api/markets/category";
            params.append("category", category);

        } else  {
            url = "/api/markets";
        }

        params.append("page", page);
        params.append("size", 3); // 페이지 당 항목 수
        params.append("sort", sortBy); // 정렬 기준

        // URL과 쿼리 파라미터 조합
        const fullUrl = `${url}?${params.toString()}`;

        $.ajax({
            url: fullUrl,
            type: "GET",
            success: function(data) {
                console.log(data);
                let contentHtml = `<h2>시장 관리</h2>`;

                // 시장이 없을 경우
                if (data.content.length === 0) {
                    $(".marketList-contents").html(`
                        <h2>시장 관리</h2>
                        <p>시장이 존재하지않습니다</p>
                    `);
                    return; // 함수 실행 종료
                }

                // 시장 목록 테이블 생성
                let marketTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>시장 이름</th>
                                <th>시장 주소</th>
                                <th>조회수</th>
                                <th>좋아요 수</th>
                                <th>도시명</th>
                                <th>수정</th>
                                <th>삭제</th>
                            </tr>
                        </thead>
                        <tbody>`;

                // 각 시장을 테이블에 추가
                data.content.forEach(function(market) {
                    let marketUrl = "/admin/markets/" + market.marketNo;
                    marketTable += `
                        <tr>
                            <td><a href="${marketUrl}" class="market-title">${market.marketName}</a></td>
                            <td>${market.marketAddr}</td>
                            <td>${market.viewCount}</td>
                            <td>${market.likes}</td>
                            <td>${market.category}</td>
                            <td>
                                <button class="update-market" data-market-no="${market.marketNo}">
                                    <a href="/admin/markets/u/${market.marketNo}">수정</a>
                                </button>
                            </td>
                            <td>
                                <button class="delete-market" data-market-no="${market.marketNo}">삭제</button>
                            </td>
                        </tr>`;
                });

                marketTable += `
                        </tbody>
                    </table>`;

                contentHtml += marketTable;
                $(".marketList-contents").html(contentHtml);

                // 삭제 버튼 클릭 이벤트 핸들러
                $(".delete-market").on("click", function() {
                    const marketNo = $(this).data("market-no");
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
                                            // 삭제 후 현재 페이지 다시 로드
                                            loadMarketList(currentPage, searchQuery, selectedCategory);
                                            $('#verifyPwModal').hide();
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

                // 페이지네이션 컨트롤 추가
                renderPagination(data.totalPages);
            },
            error: function(xhr) {
                $(".marketList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

    // 카테고리 버튼 클릭 이벤트
    $(document).on('click', '.category-btn', function() {
        selectedCategory = $(this).data('category');
        currentPage = 0; // 페이지를 0으로 초기화
        searchQuery = ""; // 검색어 초기화
        $("#searchInput").val('');
        sortBy = "marketName,asc";
        loadMarketList(currentPage, searchQuery, selectedCategory);
    });

    // 검색 버튼 클릭 이벤트
    $("#searchButton").click(function() {
        searchQuery = $("#searchInput").val();
        currentPage = 0; // 검색 시 페이지를 0으로 초기화
        loadMarketList(currentPage, searchQuery, selectedCategory);
    });

    // 엔터 키로 검색
    $("#searchInput").keypress(function(e) {
        if (e.which == 13) { // Enter 키
            $("#searchButton").click();
        }
    });

    // 모달 닫기
    $('.close').on('click', function() {
        $('#verifyPwModal').hide();
    });

    // 페이지네이션을 생성하는 함수
    function renderPagination(totalPages) {
        const paginationContainer = $(".pagination");
        paginationContainer.empty();

        // 현재 페이지를 기준으로 현재 그룹 계산
        const currentGroup = Math.floor(currentPage / groupSize);

        // 처음 버튼
        paginationContainer.append('<button class="first-group">처음</button>');

        // 이전 그룹 버튼
        if (currentGroup > 0) {
            paginationContainer.append('<button class="prev-group">이전</button>');
        }

        // 페이지 버튼
        const startPage = currentGroup * groupSize;
        const endPage = Math.min(startPage + groupSize - 1, totalPages - 1);

        for (let i = startPage; i <= endPage; i++) {
            const isActive = i === currentPage;
            const buttonClass = isActive ? 'active' : '';
            paginationContainer.append(`
                <button class="page-btn ${buttonClass}" data-page="${i}">${i + 1}</button>
            `);
        }

        // 다음 그룹 버튼
        if (endPage < totalPages - 1) {
            paginationContainer.append('<button class="next-group">다음</button>');
        }

        // 끝 버튼
        paginationContainer.append('<button class="last-group">끝</button>');

        // 페이지 버튼 클릭 이벤트
        $(".page-btn").click(function() {
            const newPage = $(this).data("page");
            currentPage = newPage;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 클릭한 버튼에 active 클래스 추가
            $(this).addClass('active');
            loadMarketList(newPage, searchQuery, selectedCategory);
        });

        // 다음 버튼 클릭 이벤트
        $(".next-group").click(function() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                // 모든 페이지 버튼의 active 클래스 제거
                $(".page-btn").removeClass('active');
                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                loadMarketList(currentPage, searchQuery, selectedCategory);
            }
        });

        // 이전 버튼 클릭 이벤트
        $(".prev-group").click(function() {
            if (currentPage > 0) {
                currentPage--;
                // 모든 페이지 버튼의 active 클래스 제거
                $(".page-btn").removeClass('active');
                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                loadMarketList(currentPage, searchQuery, selectedCategory);
            }
        });

        // 처음 버튼 클릭 이벤트
        $(".first-group").click(function() {
            currentPage = 0;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 첫 번째 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadMarketList(currentPage, searchQuery, selectedCategory);
        });

        // 끝 버튼 클릭 이벤트
        $(".last-group").click(function() {
            currentPage = totalPages - 1;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 마지막 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadMarketList(currentPage, searchQuery, selectedCategory);
        });
    }
});
