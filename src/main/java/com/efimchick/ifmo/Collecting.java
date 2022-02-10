

package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Collecting {

    int sum(IntStream stream) {
        return stream
                .sum();
    }


    int production(IntStream stream) {
        return stream
                .reduce(1, (x, y) -> x * y);
    }

    int oddSum(IntStream stream) {
        return stream
                .filter(x -> (x % 2 != 0))
                .sum();
    }

    Map<Integer, Integer> sumByRemainder(int x, IntStream stream) {
        return stream
                .boxed()
                .collect(Collectors.toMap(k -> k % x, v -> v, Integer::sum));
    }


    Map<Person, Double> totalScores(Stream<CourseResult> stream) {
        List<CourseResult> list = stream.collect(Collectors.toList());

        long counter = list.stream().map(CourseResult::getTaskResults).flatMap(map -> map.keySet().stream()).distinct().count();

        return list.stream().collect(Collectors
                .toMap(k -> k.getPerson(),
                        v -> v.getTaskResults()
                                .values()
                                .stream()
                                .mapToDouble(e -> e)
                                .sum() / counter
                )
        );
    }

    double averageTotalScore(Stream<CourseResult> stream) {
        List<CourseResult> list = stream.collect(Collectors.toList());

        long counter = list.stream()
                .map(CourseResult::getTaskResults)
                .flatMap(map -> map.keySet().stream())
                .distinct()
                .count();

        long people = list.size();

        return list.stream()
                .mapToDouble(e -> e.getTaskResults()
                        .values()
                        .stream()
                        .mapToDouble(v -> v)
                        .sum())
                .sum() / (counter * people);
    }

    Map<String, Double> averageScoresPerTask(Stream<CourseResult> stream) {
        List<CourseResult> list = stream.collect(Collectors.toList());

        double people = list.size();

        return list.stream()
                .map(CourseResult::getTaskResults)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue().doubleValue() / people, Double::sum, HashMap::new));

    }

    Map<Person, String> defineMarks(Stream<CourseResult> stream) {
        List<CourseResult> list = stream.collect(Collectors.toList());

        long counter = list.stream().map(CourseResult::getTaskResults).flatMap(map -> map.keySet().stream()).distinct().count();

        return list.stream().collect(Collectors
                .toMap(k -> k.getPerson(),
                        v -> Marks.getMark(v.getTaskResults()
                                .values()
                                .stream()
                                .mapToDouble(e -> e)
                                .sum() / counter).toString()
                )
        );
        /*
        if you need sorted map
                .entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                */
    }

    String easiestTask(Stream<CourseResult> stream) {


        List<CourseResult> list = stream.collect(Collectors.toList());

        double people = list.size();


        return list.stream()
                .map(CourseResult::getTaskResults)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(k -> k.getKey().toString(), v -> v.getValue().doubleValue() / people, Double::sum, HashMap::new))
                .entrySet()
                .stream()
                .max((o1, o2) -> (int) Math.floor(o1.getValue() - o2.getValue()))
                .get()
                .getKey();
    }

    public Collector<CourseResult, Map<Person, Map<String, Integer>>, String> printableStringCollector() {
        return new Collector<CourseResult, Map<Person, Map<String, Integer>>, String>() {
            @Override
            public Supplier<Map<Person, Map<String, Integer>>> supplier() {
                return () -> new TreeMap<>(Comparator.comparing(Person::getLastName));
            }

            @Override
            public BiConsumer<Map<Person, Map<String, Integer>>, CourseResult> accumulator() {
                return (map, courseResult) -> map.put(courseResult.getPerson(), courseResult.getTaskResults());
            }

            @Override
            public BinaryOperator<Map<Person, Map<String, Integer>>> combiner() {
                return (map1, map2) -> {
                    map1.putAll(map2);
                    return map1;
                };
            }

            @Override
            public Function<Map<Person, Map<String, Integer>>, String> finisher() {
                return map -> String.valueOf(parser(map));
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.UNORDERED);
            }
        };
    }

    private StringBuilder parser(Map<Person, Map<String, Integer>> map) {
        StringBuilder stringBuilder = new StringBuilder();

        int lengthOfLongestName = getLengthLongestName(map);

        List<String> sortedCourses = map.values()
                .stream()
                .flatMap(m -> m.keySet().stream())
                .distinct().sorted().collect(Collectors.toList());
        System.out.println(sortedCourses);

        stringBuilder.append("Student").append(getSpaces(lengthOfLongestName - 6)).append("| ");

        for (String sortedCours : sortedCourses) {
            stringBuilder.append(sortedCours).append(" | ");
        }

        stringBuilder.append("Total | ").append("Mark |\n");

        for (Map.Entry<Person, Map<String, Integer>> courses : map.entrySet()) {
            stringBuilder
                    .append(courses.getKey().getLastName())
                    .append(" ")
                    .append(courses.getKey().getFirstName())
                    .append(getSpaces(lengthOfLongestName - courses.getKey().getFirstName().length() - courses.getKey().getLastName().length() - 1))
                    .append(" | ");
            for (String sortedCours : sortedCourses) {

                if (courses.getValue().containsKey(sortedCours)) {
                    stringBuilder          //append number of spaces
                            .append(getSpaces(sortedCours.length() - courses.getValue().get(sortedCours).toString().length()))
                            .append(courses.getValue().get(sortedCours))
                            .append(" | ");
                } else {
                    stringBuilder
                            .append(getSpaces(sortedCours.length() - 1))
                            .append("0")
                            .append(" | ");
                }

            }
            stringBuilder
                    .append(getTotalScore(courses.getValue(), sortedCourses.size()))
                    .append(" |    ")
                    .append(Marks.getMark(Double.parseDouble(getTotalScore(courses.getValue(), sortedCourses.size()))).toString())
                    .append(" |\n");

        }
        stringBuilder.append("Average").append(getSpaces(lengthOfLongestName - 6)).append("|").append(getAverage(map, sortedCourses));
        return stringBuilder;
    }

    private String getAverage(Map<Person, Map<String, Integer>> coursesMap, List<String> sortedCourses) {
        StringBuilder sb = new StringBuilder();
        long numberOfCourses = coursesMap.values().stream()
                .mapToLong(map -> map.keySet().size()).count();

        Map<String, Double> mapOfCourses = coursesMap.values().stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, value -> Double.valueOf(value.getValue()), (v1, v2) -> v1 = v1 + v2));

        mapOfCourses.replaceAll((k, v) -> v / numberOfCourses);

        for (int i = 0; i < mapOfCourses.values().size(); i++) {
            sb          //append number of spaces
                    .append(getSpaces(sortedCourses.get(i).length() - 4))
                    .append(String.format(Locale.US, "%.2f", mapOfCourses.get(sortedCourses.get(i))))
                    .append(" |");

        }
        sb
                .append(" ")
                .append(getTotalScoreDouble(mapOfCourses))
                .append(" |    ")
                .append(Marks.getMark(Double.parseDouble(getTotalScoreDouble(mapOfCourses))).toString())
                .append(" |");
        return sb.toString();
    }

    private String getSpaces(int length) {
        return " ".repeat(Math.max(0, length));
    }

    private int getLengthLongestName(Map<Person, Map<String, Integer>> map) {
        return map.keySet()
                .stream()
                .mapToInt(person -> person.getLastName().length() + person.getFirstName().length() + 1)
                .max().getAsInt();
    }

    private String getTotalScore(Map<String, Integer> courseStream, int numberOfCourses) {
        double sum = courseStream.values().stream().mapToDouble(Integer::doubleValue).sum();
        return String.format(Locale.US, "%.2f", sum / numberOfCourses);

    }

    private String getTotalScoreDouble(Map<String, Double> courseStream) {
        long numberOfCourses = courseStream.keySet().stream().distinct().count();
        double sum = courseStream.values().stream().mapToDouble(Double::doubleValue).sum();
        return String.format(Locale.US, "%.2f", sum / numberOfCourses);

    }


    /*
    public interface Collector<T, A, R> {
        Supplier<A> supplier();
        BiConsumer<A, T> accumulator();
        Function<A, R> finisher();
        BinaryOperator<A> combiner();
        Set<Characteristics> characteristics();
    }
     */

    enum Marks {
        F(59),
        E(67),
        D(74),
        C(82),
        B(90),
        A(100);

        Marks(int mark) {
            this.mark = mark;
        }

        private double mark;

        public static Marks getMark(double mark) {

            for (Marks i : Marks.values()) {
                if (i.mark >= Math.floor(mark)) {
                    return i;
                }
            }
            return Marks.A;
        }
    }
}

