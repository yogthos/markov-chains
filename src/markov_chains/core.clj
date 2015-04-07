(ns markov-chains.core
  (:require [clojure.string :as s]))

;;generating the chains
;Split a body of text into tokens

;Build a frequency table.

;The frequency table is a map where each word is a key
;the value is a structure that describes all words that follow the word ranked by frequency

;;generating the output from chains
;select a random key from the frequency table as the initial state
;then select the next word using weighed random selection based on the frequency from the words in the chain
;associated with the word

;repeat until the end condition is met

(defn chain-word [chain word]
  (update-in chain [word] (fnil inc 0)))

(defn chain-words [frequency-table [last-word word next-word]]
  (if word
    (-> frequency-table
        (update-in [last-word] chain-word word)
        (update-in [word] chain-word next-word))
    frequency-table))

(defn weigh-by-frequency [chain]
  (let [total (->> chain (map second) (reduce + 0))]
    (when (pos? total)
      {:total total
       :chain
       (->> chain
            (sort-by second)
            reverse
            (reductions (fn [[_ i] [w n]] [w (+ i n)])))})))

(defn partition-text [line]
  (->> (s/split line #" ") (map s/trim) (remove empty?) (partition 3 1)))

(defn parse-text [frequency-table text]
  (reduce chain-words frequency-table (partition-text text)))

(defn clean-text [text]
  (-> text
     (s/replace #"\n" " ")
     (s/replace #"[^a-zA-Z0-9'. ]" "")))

(defn generate-frequencies [file]
  (->> (slurp file)
       (clean-text)
       (parse-text {})
       (map #(update-in % [1] weigh-by-frequency))
       (remove #(nil? (second %)))
       (into {})))

(defn select-by-frequency [{:keys [total chain]}]
  (let [position (rand-int total)]
    (->> chain (drop-while #(< (second %) position)) ffirst)))

(defn random-word [frequency-table]
  (rand-nth (seq frequency-table)))

(defn select-word [frequency-table chain]
  (if chain
    (let [word (select-by-frequency chain)]
      [word (frequency-table word)])
    (random-word frequency-table)))

(defn populate-starting-words [frequency-table]
  (filter #(Character/isUpperCase (first %)) (keys frequency-table)))

(defn random-starting-word [{:keys [frequency-table starting-words]}]
  (let [word (rand-nth starting-words)]
    [word (get frequency-table word)]))

(defn generate-text [{:keys [frequency-table] :as chains}]
  (loop [[word chain] (random-starting-word chains)
         result []]
    (if
     (and (not-empty result) (some #{(-> result last last)} [\. \? \! \;]))
     (clojure.string/join " " result)
     (recur (select-word frequency-table chain)
            (conj result word)))))

(defn create-chains [file]
  (let [frequency-table (generate-frequencies file)]
    {:frequency-table frequency-table
     :starting-words (populate-starting-words frequency-table)}))

;; usage example
(comment
  (let [chains (create-chains "corpus.txt")]
    (generate-text chains)))

