package com.cinema.model;

/**
 * 电影类型枚举
 * 提供类型安全的电影类型定义
 */
public enum MovieGenre {
    ACTION("动作片"),
    COMEDY("喜剧片"),
    DRAMA("剧情片"),
    HORROR("恐怖片"),
    ROMANCE("爱情片"),
    SCIENCE_FICTION("科幻片"),
    ANIMATION("动画片"),
    THRILLER("惊悚片"),
    DOCUMENTARY("纪录片"),
    FANTASY("奇幻片"),
    MYSTERY("悬疑片"),
    ADVENTURE("冒险片"),
    CRIME("犯罪片"),
    FAMILY("家庭片"),
    MUSICAL("音乐片"),
    WAR("战争片"),
    WESTERN("西部片"),
    BIOGRAPHY("传记片"),
    SPORT("体育片");

    private final String description;

    MovieGenre(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据字符串描述获取对应的枚举值
     * @param description 描述文本
     * @return 对应的枚举值，如果找不到则返回DRAMA
     */
    public static MovieGenre fromDescription(String description) {
        if (description == null) {
            return DRAMA; // 默认值
        }
        
        for (MovieGenre genre : values()) {
            if (genre.description.equals(description) || genre.name().equalsIgnoreCase(description)) {
                return genre;
            }
        }
        return DRAMA; // 默认值
    }

    @Override
    public String toString() {
        return description;
    }
}