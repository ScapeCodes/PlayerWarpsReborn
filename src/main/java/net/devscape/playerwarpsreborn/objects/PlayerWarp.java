package net.devscape.playerwarpsreborn.objects;

import org.bukkit.Location;

import java.util.UUID;

public class PlayerWarp {

    private String name;
    private UUID owner;
    private Location location;
    private String description;
    private String password;
    private boolean passwordMode;
    private boolean isLocked;

    private String category;
    private String icon;
    private int visits;

    // Rating system fields
    private int totalRatingScore; // Sum of all rating values
    private int numberOfRatings; // Number of ratings submitted

    public PlayerWarp(String name, UUID owner, Location location, String description, String password, boolean passwordMode, boolean isLocked, String category, String icon, int visits, int totalRatingScore, int numberOfRatings) {
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.description = description;
        this.password = password;
        this.passwordMode = passwordMode;
        this.isLocked = isLocked;
        this.category = category;
        this.icon = icon;
        this.visits = visits;
        this.totalRatingScore = totalRatingScore;
        this.numberOfRatings = numberOfRatings;
    }

    // Getters and setters for the rating system
    public int getTotalRatingScore() {
        return totalRatingScore;
    }

    public int getNumberOfRatings() {
        return numberOfRatings;
    }

    public double getAverageRating() {
        if (numberOfRatings == 0) {
            return 0; // No ratings yet
        }
        return (double) totalRatingScore / numberOfRatings;
    }

    public int getStarRating() {
        return (int) Math.round(getAverageRating());
    }

    // Method to add a new rating
    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.totalRatingScore += rating;
        this.numberOfRatings++;
    }

    // Existing getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPasswordMode() {
        return passwordMode;
    }

    public void setPasswordMode(boolean passwordMode) {
        this.passwordMode = passwordMode;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getStars() {
        int starCount = getStarRating(); // Rounded star rating (1 to 5)
        StringBuilder stars = new StringBuilder();

        // Append full stars
        for (int i = 0; i < starCount; i++) {
            stars.append("★");
        }

        // Append empty stars for remaining slots
        for (int i = starCount; i < 5; i++) {
            stars.append("☆");
        }

        return stars.toString();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getVisits() {
        return visits;
    }

    public void setVisits(int visits) {
        this.visits = visits;
    }

    public void addVisits(int amount) {
        visits = visits + amount;
    }

    public void removeVisits(int amount) {
        visits = visits - amount;
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }
}