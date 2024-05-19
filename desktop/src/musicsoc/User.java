using System.Collections.Generic;
using System;

import java.util.*;

class User
{
    private String username;
    private List<Track> favoriteTracks;

    public User(String username)
    {
        this.username = username;
        this.favoriteTracks = new ArrayList<>();
    }

    public void addFavoriteTrack(Track track)
    {
        favoriteTracks.add(track);
    }

    public List<Track> getFavoriteTracks()
    {
        return favoriteTracks;
    }
}

class Track
{
    private String title;
    private String artist;

    public Track(String title, String artist)
    {
        this.title = title;
        this.artist = artist;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }
}

public class CollaborativeFiltering
{
    private List<User> users;

    public CollaborativeFiltering(List<User> users)
    {
        this.users = users;
    }

    public List<Track> recommendTracks(User targetUser)
    {
        Map<Track, Integer> trackScores = new HashMap<>();

        for (User user : users)
        {
            if (user != targetUser)
            {
                for (Track track : user.getFavoriteTracks())
                {
                    if (!targetUser.getFavoriteTracks().contains(track))
                    {
                        trackScores.put(track, trackScores.getOrDefault(track, 0) + 1);
                    }
                }
            }
        }

        List<Track> recommendedTracks = new ArrayList<>();

        for (Map.Entry<Track, Integer> entry : trackScores.entrySet())
        {
            if (entry.getValue() > 1)
            { // Трек рекомендується, якщо його вподобали більше одного користувача (можна налаштувати параметр)
                recommendedTracks.add(entry.getKey());
            }
        }

        return recommendedTracks;
    }

    public static void main(String[] args)
    {
        // Створення користувачів та їх улюблених треків
        User user1 = new User("user1");
        user1.addFavoriteTrack(new Track("Track1", "Artist1"));
        user1.addFavoriteTrack(new Track("Track2", "Artist2"));
        user1.addFavoriteTrack(new Track("Track3", "Artist3"));

        User user2 = new User("user2");
        user2.addFavoriteTrack(new Track("Track2", "Artist2"));
        user2.addFavoriteTrack(new Track("Track3", "Artist3"));
        user2.addFavoriteTrack(new Track("Track4", "Artist4"));

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        // Створення екземпляру колаборативного фільтрування
        CollaborativeFiltering collaborativeFiltering = new CollaborativeFiltering(users);

        // Рекомендація треків для певного користувача
        List<Track> recommendedTracks = collaborativeFiltering.recommendTracks(user1);
        System.out.println("Recommended tracks for " + user1.getUsername() + ": " + recommendedTracks);
    }
}
