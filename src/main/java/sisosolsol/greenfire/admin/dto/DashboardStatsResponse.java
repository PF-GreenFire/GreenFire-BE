package sisosolsol.greenfire.admin.dto;

public record DashboardStatsResponse(
        long totalUsers,
        long pendingReports,
        long handledReports,
        int activeChallenges,
        long recentActivities
) {}
