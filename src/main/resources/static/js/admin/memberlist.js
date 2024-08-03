$(document).ready(function() {
    let currentPage = 0; // 현재 페이지 초기화
    const groupSize = 5; // 한 그룹에 보여줄 페이지 수

    const roleDropdown = $('#role');
    const memberListContainer = $('.memberList-contents');

    loadMembers(currentPage);

    // 권한 목록이 변경될 때 호출되는 함수
    roleDropdown.on('change', function() {
        currentPage = 0; // 페이지 초기화
        loadMembers(currentPage);
    });

    // 회원 목록을 가져오는 함수
    function loadMembers(page) {
        const selectedRole = roleDropdown.val();
        let url = `/api/members?page=${page}&size=3&sort=createTime`;

        if (selectedRole && selectedRole !== 'all') {
            url = `/api/members/admin/role?role=${selectedRole}&page=${page}&size=3&sort=createTime`;
        }

        $.ajax({
            url: url,
            type: "GET",
            success: function(data) {
                console.log(data);

                // 회원 목록 제목을 유지
                let contentHtml = `<h2>회원 목록</h2>`;

                // 회원이 없을 경우
                if (data.content.length === 0) {
                    $(".memberList-contents").html(`
                        <h2>회원 목록</h2>
                        <p>회원이 없습니다</p>
                        <button id="viewAllMembersBtn">전체 회원 목록 보기</button>
                    `);
                    // 전체 회원 목록 보기 버튼 클릭 이벤트 핸들러 추가
                    $("#viewAllMembersBtn").click(function() {
                        // 페이지 새로고침
                        location.reload();
                    });
                    return; // 함수 실행 종료
                }

                // 회원 목록을 성공적으로 가져오면 테이블을 생성
                let memberTable = `
                    <table>
                        <thead>
                            <tr>
                                <th>회원 ID</th>
                                <th>이메일</th>
                                <th>닉네임</th>
                                <th>권한</th>
                                <th>가입일</th>
                                <th>상세보기</th>
                            </tr>
                        </thead>
                        <tbody>`;

                data.content.forEach(function(member) {
                    memberTable += `
                        <tr>
                            <td>${member.memberId}</td>
                            <td>${member.memberEmail}</td>
                            <td>${member.nicknameWithRandomTag}</td>
                            <td>${member.role}</td>
                            <td>${member.createTime}</td>
                            <td>
                                <button class="detail-member" data-shop-no="${member.memberNo}">
                                    <a href="/admin/memberinfo/${member.memberNo}">상세보기</a>
                                </button>
                            </td>
                        </tr>`;
                });

                memberTable += `
                        </tbody>
                    </table>`;

                // 전체 내용 설정
                contentHtml += memberTable;
                $(".memberList-contents").html(contentHtml); // memberList-contents에 테이블 추가

                // 페이지네이션 컨트롤 추가
                renderPagination(data.totalPages);
            },
            error: function(xhr) {
                // 오류 발생 시 메시지 표시
                $(".memberList-contents").html(`<p>${xhr.responseText}</p>`);
            }
        });
    }

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
            loadMembers(newPage);
        });

        // 다음 버튼 클릭 이벤트
        $(".next-group").click(function() {
            if (currentPage < totalPages - 1) {
                currentPage++;
                // 모든 페이지 버튼의 active 클래스 제거
                $(".page-btn").removeClass('active');
                // 클릭한 페이지에 해당하는 버튼에 active 클래스 추가
                $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
                loadMembers(currentPage);
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
                loadMembers(currentPage);
            }
        });

        // 처음 버튼 클릭 이벤트
        $(".first-group").click(function() {
            currentPage = 0;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 첫 번째 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadMembers(currentPage);
        });

        // 끝 버튼 클릭 이벤트
        $(".last-group").click(function() {
            currentPage = totalPages - 1;
            // 모든 페이지 버튼의 active 클래스 제거
            $(".page-btn").removeClass('active');
            // 마지막 페이지 버튼에 active 클래스 추가
            $(`.page-btn[data-page="${currentPage}"]`).addClass('active');
            loadMembers(currentPage);
        });
    }
});
