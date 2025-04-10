package org.example.repository;

import org.example.dto.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InterviewRepository {
    private final Map<String, Deque<Question>> userQuestions = new ConcurrentHashMap<>();
    private final int maxQuestions;

    public InterviewRepository(@Value("${interview.max-questions}") int maxQuestions) {
        this.maxQuestions = maxQuestions;
    }

    public void startInterview(String userName) {
        userQuestions.putIfAbsent(userName, new LinkedList<>());
    }

    public void addQuestion(String userName, String question) {
        Question dto = new Question();
        dto.setQuestion(question);
        userQuestions.computeIfAbsent(userName, k -> new LinkedList<>()).add(dto);
    }

    public void addAnswer(String userName, String answer) {
        if (!userQuestions.containsKey(userName)) {
            throw new IllegalStateException("Пожалуйста, начните интервью командой /start");
        }

        Question question = userQuestions.get(userName).peekLast();
        if (question != null) {
            question.setAnswer(answer);
        }
    }

    public Deque<Question> finishInterview(String userName) {
        return userQuestions.remove(userName);
    }

    public int getUserQuestionsCount(String userName) {
        return userQuestions.getOrDefault(userName, new LinkedList<>()).size();
    }

    public int getMaxQuestions() {
        return maxQuestions;
    }
}

//@Repository
//public class InterviewRepository {
//
//    private final Map<String, Deque<Question>> userQuestions = new HashMap<>();
//
//    public void addQuestion(String userName, String question) {
//        Question dto = new Question();
//        dto.setQuestion(question);
//        userQuestions.computeIfAbsent(userName, k -> new LinkedList<>()).add(dto);
//    }
//
//    public void addAnswer(String userName, String answer) {
//        if (userQuestions.containsKey(userName)) {
//            Question question = userQuestions.get(userName).peekLast();
//            if (question != null) {
//                question.setAnswer(answer);
//            } else {
//                throw new IllegalStateException("There is no question being answered for user " + userName);
//            }
//        } else {
//            throw new IllegalStateException("There is no interview session starter for user " + userName);
//        }
//    }
//
//    public Deque<Question> finishInterview(String userName) {
//        return userQuestions.remove(userName);
//    }
//
//    public int getUserQuestions(String userName) {
//        return userQuestions.get(userName).size();
//    }
//}