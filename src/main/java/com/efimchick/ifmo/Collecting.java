package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
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

    Collector printableStringCollector() {


        return new Collector() {
            @Override
            public Supplier<Object> supplier() {

                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<String>, CourseResult> accumulator() {

                return new BiConsumer<List<String>, CourseResult>() {
                    @Override
                    public void accept(List<String> strings, CourseResult courseResult) {
                        strings.add(courseResult.toString());
                    }
                };
            }

            @Override
            public BinaryOperator<List<String>> combiner() {
              return (list1, list2) -> {
                  list1.addAll(list2);
                  return list1;
              };
            }

            @Override
            public Function<Object, Object> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.UNORDERED);
            }
        };

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
                if (i.mark >= mark) {
                    return i;
                }
            }
            return Marks.A;
        }
    }
}
