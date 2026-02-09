package sisosolsol.greenfire.admin.dto;

import java.util.List;

public record MemberPageResponse(
        List<MemberResponse> members,
        long total,
        int page,
        int size,
        boolean hasMore
) {}
