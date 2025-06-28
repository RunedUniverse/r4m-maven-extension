/*
 * Copyright © 2025 VenaNocta (venanocta@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.runeduniverse.tools.maven.r4m.pem.api;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a predicate (boolean-valued function) of one argument.
 *
 * <p>
 * This is a <a href="package-summary.html">functional interface</a> whose
 * functional method is {@link #test(Object, Object)}.
 *
 * @param <M> the type of the model the predicate belongs to
 * @param <T> the type of the input to the predicate
 */
@FunctionalInterface
public interface ModelPredicate<M, T> {

	/**
	 * Evaluates this predicate on the given argument.
	 *
	 * @param m the model the predicate belongs to
	 * @param t the input argument
	 * @return {@code true} if the input argument matches the predicate, otherwise
	 *         {@code false}
	 */
	public boolean test(M m, T t);

	/**
	 * Returns a composed predicate that represents a short-circuiting logical AND
	 * of this predicate and another. When evaluating the composed predicate, if
	 * this predicate is {@code false}, then the {@code other} predicate is not
	 * evaluated.
	 *
	 * <p>
	 * Any exceptions thrown during evaluation of either predicate are relayed to
	 * the caller; if evaluation of this predicate throws an exception, the
	 * {@code other} predicate will not be evaluated.
	 *
	 * @param other a predicate that will be logically-ANDed with this predicate
	 * @return a composed predicate that represents the short-circuiting logical AND
	 *         of this predicate and the {@code other} predicate or this predicate
	 *         if the {@code other} is null;
	 */
	public default ModelPredicate<M, T> and(ModelPredicate<? super M, ? super T> other) {
		if (other == null)
			return this;
		return (m, t) -> test(m, t) && other.test(m, t);
	}

	@SafeVarargs
	public static <M, T> ModelPredicate<M, T> and(ModelPredicate<? super M, ? super T>... predicates) {
		Objects.requireNonNull(predicates);
		ModelPredicate<M, T> p = null;
		for (int i = 0; i < predicates.length; i++) {
			@SuppressWarnings("unchecked")
			final ModelPredicate<M, T> other = (ModelPredicate<M, T>) predicates[i];
			if (other == null)
				continue;
			if (p == null)
				p = other;
			p = p.and(other);
		}
		return p;
	}

	/**
	 * Returns a predicate that represents the logical negation of this predicate.
	 *
	 * @return a predicate that represents the logical negation of this predicate
	 */
	public default ModelPredicate<M, T> negate() {
		return (m, t) -> !test(m, t);
	}

	/**
	 * Returns a composed predicate that represents a short-circuiting logical OR of
	 * this predicate and another. When evaluating the composed predicate, if this
	 * predicate is {@code true}, then the {@code other} predicate is not evaluated.
	 *
	 * <p>
	 * Any exceptions thrown during evaluation of either predicate are relayed to
	 * the caller; if evaluation of this predicate throws an exception, the
	 * {@code other} predicate will not be evaluated.
	 *
	 * @param other a predicate that will be logically-ORed with this predicate
	 * @return a composed predicate that represents the short-circuiting logical OR
	 *         of this predicate and the {@code other} predicate or this predicate
	 *         if the {@code other} is null;
	 */
	public default ModelPredicate<M, T> or(ModelPredicate<? super M, ? super T> other) {
		if (other == null)
			return this;
		return (m, t) -> test(m, t) || other.test(m, t);
	}

	@SafeVarargs
	public static <M, T> ModelPredicate<M, T> or(ModelPredicate<? super M, ? super T>... predicates) {
		Objects.requireNonNull(predicates);
		ModelPredicate<M, T> p = null;
		for (int i = 0; i < predicates.length; i++) {
			@SuppressWarnings("unchecked")
			final ModelPredicate<M, T> other = (ModelPredicate<M, T>) predicates[i];
			if (other == null)
				continue;
			if (p == null)
				p = other;
			p = p.or(other);
		}
		return p;
	}

	/**
	 * Returns a predicate that tests if two model arguments are equal according to
	 * {@link Objects#equals(Object, Object)}.
	 *
	 * @param <M>       the type of the model the predicate belongs to
	 * @param <T>       the type of arguments to the predicate
	 * @param targetRef the object reference with which to compare for equality,
	 *                  which may be {@code null}
	 * @return a predicate that tests if two arguments are equal according to
	 *         {@link Objects#equals(Object, Object)}
	 */
	public static <M, T> ModelPredicate<M, T> isEqualModel(Object targetRef) {
		return (null == targetRef) ? (object, t) -> Objects.isNull(object) : (object, t) -> targetRef.equals(object);
	}

	/**
	 * Returns a predicate that tests if two arguments are equal according to
	 * {@link Objects#equals(Object, Object)}.
	 *
	 * @param <M>       the type of the model the predicate belongs to
	 * @param <T>       the type of arguments to the predicate
	 * @param targetRef the object reference with which to compare for equality,
	 *                  which may be {@code null}
	 * @return a predicate that tests if two arguments are equal according to
	 *         {@link Objects#equals(Object, Object)}
	 */
	public static <M, T> ModelPredicate<M, T> isEqual(Object targetRef) {
		return (null == targetRef) ? (m, object) -> Objects.isNull(object) : (m, object) -> targetRef.equals(object);
	}

	/**
	 * Returns a model-predicate that wraps a simple predicate.
	 *
	 * @param <M>          the type of the model the predicate belongs to
	 * @param <T>          the type of arguments to the predicate
	 * @param Predicate<T> predicate without dependence on a model
	 * @return a model-predicate that wraps a simple predicate
	 */
	public static <M, T> ModelPredicate<M, T> wrap(Predicate<T> predicate) {
		return (m, t) -> predicate.test(t);
	}

}
