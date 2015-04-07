# markov-chains

a Clojure implementation of [Markov chains](https://en.wikipedia.org/wiki/Markov_chain)

## Usage

```clojure
(use 'markov-chains.core)

(let [chains (create-chains "corpus.txt")]
  (generate-text chains))
```

## License

Copyright © 2015 Dmitri Sotnkov

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
