(ns tetris.geometry)

(def rotation-matrices
  {:I {[[0 0] [0 0] [0 0] [0 0]]
       [[2 -1] [1 0] [0 1] [-1 2]]
       [[2 -1] [1 0] [0 1] [-1 2]]
       [[0 1] [0 1] [0 1] [0 1]]
       [[0 1] [0 1] [0 1] [0 1]]
       [[1 -1] [0 0] [-1 1] [-2 2]]
       [[1 -1] [0 0] [-1 1] [-2 2]]
       [[0 0] [0 0] [0 0] [0 0]]}
   :J {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [2 0] [0 0] [-1 1]]
       [[1 -1] [2 0] [0 0] [-1 1]]
       [[2 0] [2 2] [0 0] [-2 0]]
       [[2 0] [2 2] [0 0] [-2 0]]
       [[1 1] [0 2] [0 0] [-1 -1]]
       [[1 1] [0 2] [0 0] [-1 -1]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :L {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [-1 1] [0 2]]
       [[1 -1] [0 0] [-1 1] [0 2]]
       [[2 0] [0 0] [-2 0] [-2 2]]
       [[2 0] [0 0] [-2 0] [-2 2]]
       [[1 1] [0 0] [-1 -1] [-2 0]]
       [[1 1] [0 0] [-1 -1] [-2 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :O {
       [[0 0] [0 0] [0 0] [0 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :S {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [1 1] [0 2]]
       [[1 -1] [0 0] [1 1] [0 2]]
       [[2 0] [0 0] [0 2] [-2 2]]
       [[2 0] [0 0] [0 2] [-2 2]]
       [[1 1] [0 0] [-1 1] [-2 0]]
       [[1 1] [0 0] [-1 1] [-2 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :T {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [1 1] [-1 1]]
       [[1 -1] [0 0] [1 1] [-1 1]]
       [[2 0] [0 0] [0 2] [-2 0]]
       [[2 0] [0 0] [0 2] [-2 0]]
       [[1 1] [0 0] [-1 1] [-1 -1]]
       [[1 1] [0 0] [-1 1] [-1 -1]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :Z {
       [[0 0] [0 0] [0 0] [0 0]]
       [[2 0] [1 1] [0 0] [-1 1]]
       [[2 0] [1 1] [0 0] [-1 1]]
       [[2 2] [0 2] [0 0] [-2 0]]
       [[2 2] [0 2] [0 0] [-2 0]]
       [[0 2] [-1 1] [0 0] [-1 -1]]
       [[0 2] [-1 1] [0 0] [-1 -1]]
       [[0 0] [0 0] [0 0] [0 0]]
       }})

(defn rotate
  "Rotate `coords` by a 4-vector `rotation-matrix`."
  [coords rotation-matrix]
  (loop [result [(mapv + (first coords) (first rotation-matrix))]
         coords (rest coords)
         rotation-matrix (rest rotation-matrix)]
    (if (empty? coords)
      result
      (recur (conj result (mapv + (first coords) (first rotation-matrix)))
             (rest coords)
             (rest rotation-matrix)))))

(defn translate
  "Translate `tetromino` by `[x y]`, respecting the bounds of the playfield."
  [tetromino [x y]]
  (let [translated-coords (map #(vector (+ x (first %)) (+ y (second %))) (:coords tetromino))
        rotated-coords    (rotate translated-coords (:rotation-matrix tetromino))]
    (if (or (some #(or (neg? %) (> % 9)) (map #(first %) rotated-coords))
            (some #(> % 15) (map #(second %) rotated-coords)))
      tetromino
      (merge tetromino {:coords translated-coords}))))

(defn contiguous?
  "A set of coords `c1`` is contiguous with another set `c2` if
  on the next tick of the clock the sets would overlap."
  [c1 c2]
  (>=
      (count
       (clojure.set/intersection
        ;; have to manually translate here, to avoid bounds-checking
        (set (map #(vector (first %) (inc (second %))) c1))
        (set (keys c2))))
      1))

(defn coord-in?
  "Is `c` in `coords`?"
  [c coords]
  (> (count (clojure.set/intersection (set [c]) (set coords))) 0))
