// ... Imports ...
@GetMapping("/active-list")
public List<Map<String, Object>> getActiveAttendance() {
    List<User> users = userRepository.findAll();
    Match match = matchRepository.findByIsActiveTrue().stream().findFirst().orElse(null);

    return users.stream().map(u -> {
        Map<String, Object> map = new HashMap<>();
        map.put("id", u.getId());
        map.put("name", u.getFullName());
        map.put("isExempt", u.isExempt()); // مهم جداً للأدمن واللاعب

        if (match != null) {
            Attendance att = attendanceRepository.findAll().stream()
                .filter(a -> a.getPlayer().getId().equals(u.getId()) && a.getMatch().getId().equals(match.getId()))
                .findFirst().orElse(null);
            map.put("status", (att == null || att.getStatus() == null) ? "لم يصوت" : 
                   ("YES".equals(att.getStatus()) ? "✅ سأحضر" : "❌ معتذر"));
            map.put("paid", att != null && att.isPaid());
        } else {
            map.put("status", "لا يوجد مباراة"); map.put("paid", false);
        }
        return map;
    }).collect(Collectors.toList());
}
