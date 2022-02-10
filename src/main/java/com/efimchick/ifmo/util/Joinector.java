package com.efimchick.ifmo.util;

import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class Joinector implements Collector<CharSequence, StringJoiner, String> {

    public static Collector<CharSequence, StringJoiner, String> joinector(CharSequence delimiter) {
        return Collector.of(() -> new StringJoiner(delimiter), // supplier
                StringJoiner::add,                 // accumulator
                StringJoiner::merge,               // combiner
                StringJoiner::toString);           // finisher
    }

    public static void main(String[] args) {
        System.out.println();
    }
    private final CharSequence delimiter;

    public Joinector(CharSequence delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public Supplier<StringJoiner> supplier() {
        // The accumulator object creation.
        return () -> new StringJoiner(this.delimiter);
    }

    @Override
    public BiConsumer<StringJoiner, CharSequence> accumulator() {
        // How to add new stream elements to the accumulator object.
        return StringJoiner::add;
    }

    @Override
    public BinaryOperator<StringJoiner> combiner() {
        // How to merge different accumulator objects together.
        return StringJoiner::merge;
    }

    @Override
    public Function<StringJoiner, String> finisher() {
        // How to extract the final result.
        return StringJoiner::toString;
    }

    @Override
    public Set<Characteristics> characteristics() {
        // Special characteristics of our Collector.
        return Collections.emptySet();
    }
}

