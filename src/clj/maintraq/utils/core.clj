(ns maintraq.utils.core)


(defn remove-nils
  "Given a map with possible `nil` values, return a map with the nil values removed."
  [m]
  (not-empty
   (apply dissoc
          m
          (for [[k v] m
                :when (nil? v)]
            k))))
