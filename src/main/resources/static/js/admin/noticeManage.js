$(document).ready(function() {
    let currentPage = 0; // 현재 페이지 초기화
    const groupSize = 5; // 한 그룹에 보여줄 페이지 수

    // 페이지가 로드될 때 첫 번째 페이지의 공지사항 가져오기
    loadNotices(currentPage);

    // 공지사항을 가져오는 함수
    function loadNotices(page) {
        $.ajax({
            url: "/api/notices?page=" + page + "&size=3&sort=createTime,desc",
            type: "GET",
            success: function(data) {
                console.log(data);

                // 공지사항 목록 제목을 유지
                let contentHtml = `<h2>공지사항 목록</h2>`;

                // 공지사항이 없을 경우
                if (data.content.length === 0) {
                    $(".noticeList-contents").html(`
                        <h2>공지사항 목록</h2>
                        <p>공지사항이 없습니다</p>
                    `);
                    $(".pagination").remove(); // 페이지네이션이 있으면 제거
                    return; // 함수 실행 종료
                }

                // 공지사항 테이블 생성
                let noticeTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>공지사항 제목</th>
                                <th>작성자</th>
                                <th>작성일</th>
                                <th>조회수</th>
                                <th>수정</th>
                                <th>삭제</th>
                            </tr>
                        </thead>
                        <tbody>`;

                // 각 공지사항을 테이블에 추가
                data.content.forEach(function(notice) {
                    let noticeUrl = "/notice/" + notice.noticeNo;
                    noticeTable += `
                        <tr>
                            <td><a href="${noticeUrl}" class="notice-title">${notice.noticeTitle}</a></td>
                            <td>${notice.noticeWriter}</td>
                            <td>${notice.createTime}</td>
                            <td>${notice.viewCount}</td>
                            <td>
                                <button class="update-notice" data-notice-no="${notice.noticeNo}">수정</button>
                            </td>
                            <td>
                                <button class="delete-notice" data-notice-no="${notice.noticeNo}">삭제</button>
                            </td>
                        </tr>`;
                });

                noticeTable += `
                        </tbody>
                    </table>`;

                // 전체 내용 설정
                contentHtml += noticeTable;
                $(".noticeList-contents").html(contentHtml); // 공지사항 목록 출력

                // 삭제 버튼 클릭 이벤트 핸들러
                $(".delete-notice").on("click", function() {
                    const noticeNo = $(this).data("notice-no");
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
                                // 비밀번호가 일치할 경우 공지사항 삭제 전에 확인 팝업
                                if (confirm("정말 이 공지사항을 삭제하시겠습니까?")) {
                                    $.ajax({
                                        url: "/api/notices/" + noticeNo,
                                        type: "DELETE",
                                        success: function(response) {
                                            alert(response.message);
                                            $('#verifyPwModal').hide();
                                            // 삭제 후 현재 페이지 다시 로드
                                            loadNotices(currentPage);
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

                // 수정 버튼 클릭 이벤트 핸들러
                $(".update-notice").on("click", function() {
                    const noticeNo = $(this).data("notice-no");
                    window.location.href = "/admin/notices/" + noticeNo; // 수정 페이지로 이동
                });

                // 페이지네이션 컨트롤 추가
                renderPagination(data.totalPages);
            },
            error: function(xhr) {
                $(".noticeList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

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
            loadNotices(newPage);
        });

        // 다음 버튼 클릭 이벤트
        $(".next-group").click(function() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                // 모든 페이지 버튼의 active 클래스 제거
                $(".page-btn").removeClass('active');
                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                loadNotices(currentPage);
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
                loadNotices(currentPage);
            }
        });

        // 처음 버튼 클릭 이벤트
        $(".first-group").click(function() {
            currentPage = 0;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 첫 번째 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadNotices(currentPage);
        });

        // 끝 버튼 클릭 이벤트
        $(".last-group").click(function() {
            currentPage = totalPages - 1;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 마지막 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadNotices(currentPage);
        });
    }
});
