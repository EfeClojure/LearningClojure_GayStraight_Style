(ns godslisp.core 
  (:require [clojure.java.io :as io])
  (:import [javax.swing JFrame JPanel JLabel JTextField JButton JDialog 
            JComboBox JMenu JMenuBar JMenuItem JOptionPane JWindow]
           [java.awt.image BufferedImage] 
           [java.awt.event ActionListener WindowEvent WindowAdapter]
           [java.awt Graphics GridLayout BorderLayout FlowLayout Dimension 
            Color Font Toolkit]
           [javax.imageio ImageIO]
           [javax.swing.border TitledBorder])
  (:gen-class))

;;; This program simply displays 3 asses some of which
;;; may be male or female. The user won't know which is
;;; initially. The user then has to pick  which one he/she
;;; likes. By so doing the program can tell
;;; if the user is gay or straight. 
;;; Disclaimer: This program does not work 100% of the time 
;;; hii hii


;;; for the dialog when done selecting whether male or female
;;; and the initial splash that welcomes the user
(declare init-message-dialog 
         init-splash-holder) 

;;; We need the asses of those models. 
;;; We need to do something when the user attempts 
;;; to close the window of the app.
;;; We need to tell the user what the app is about.
;;; We need somewhere to display the lovely asses to the user.
(declare get-models-images get-window-listener 
         get-about-menu-listener get-content-pane)

;;; Both girls and boys can use the program. 
;;; Ermm ... false is used for the girls while 
;;; true is used for boys. Trannies I'm sorry.
(def gender (atom false))

;;; After the user is done selecting the asses
;;; he likes and he told he is gay, he needs to see
;;; why the program told him he is gay by seeing the full
;;; images of the owners of the asses.
(def reveal-full-images (atom false))


;;; There is a folder in the 'resources' folder of this project. 
;;; It holds separate images of the models used. The genders of each
;;; model in each file is assigned to this variable
(def genders-of-images [true false true false true false false]) 


;;; This function will get a vector of random numbers but
;;; making sure that no element in the vector is the same.
;;; This function isn't written in the best way but
;;; since we are working with just 3 randoms it is just
;;; fine.
(defn get-randoms 
  ([upper-limit]
    (get-randoms upper-limit [0 0 0]))
  ([upper-limit so-far]
    (if (or (== (so-far 0) (so-far 1))
            (== (so-far 1) (so-far 2))
            (== (so-far 0) (so-far 2)))
      (recur upper-limit [(rand-int upper-limit) 
                         (rand-int upper-limit)
                         (rand-int upper-limit)])
      so-far)))


;;; Three asses will be displayed to the user. We don't want the
;;; asses to all be the same. 
(def rand-image-indices (atom (get-randoms 7))) 

;;; This is the number of images of models we 
;;; got in our 'resources' folder
(def NUM_IMAGES 7)


;;; Both variables are necessary after the user is done selecting
;;; the asses he likes. If the gay-count is greater than the
;;; straight count, the person is confirmed gay. Nice.
(def gay-count (atom 0))
(def straight-count (atom 0))

;;; This is an array that holds the 3 answers of the asses the user
;;; likes 
(def answers (make-array Boolean/TYPE 3)) 


;;; On program startup the user will be asked if he is male or 
;;; female. Then the title of the window will be set to 
;;; show the gender selected.
(defn set-gender-get-title []
  (let [result (JOptionPane/showConfirmDialog 
                 nil "Are you male" "Gender specifier" 0)  ]
    (if (zero? result)
      (do 
        (reset! gender true)
        "GODS-male user")
      (do 
        (reset! gender false)
        "GODS-female user"))))


;;; For aesthetics and to show good taste that ahem Mister Paul Graham
;;; would like.
(defn- him-or-her []
  (if @gender "him" "her"))


;;; This is how you load images from the 'resources' folder
(defn- get-models-images []
  (for [i (range 0 NUM_IMAGES) 
        :let [new-url (io/resource (str "separateModels/Model_" i ".png"))]
        :when new-url]
    (ImageIO/read new-url)))

;;; This is just to set the icon for the application.
;;; NOthing special. 
(def cat-image (ImageIO/read (io/resource "Cat.jpg")))
(def models-images (get-models-images))

(def SCREEN_SIZE (.getScreenSize (Toolkit/getDefaultToolkit)))
(def SCREEN_WIDTH (.width SCREEN_SIZE))
(def SCREEN_HEIGHT (.height SCREEN_SIZE))

(def APP_WIDTH (* 3 (.getWidth (first models-images))))
(def APP_HEIGHT (+ (.getHeight (first models-images)) 80))

(defn get-j-frame []
  (let [menu-bar (JMenuBar.)
        about-menu (JMenu. "About")
        about-menu-item (JMenuItem. "About me")
        j-frame (JFrame.)]
    (.add about-menu about-menu-item)
    (.add menu-bar about-menu)
    (.addActionListener about-menu-item (get-about-menu-listener))
    (doto j-frame 
      (.setJMenuBar menu-bar)
      (.setBounds (/ (- SCREEN_WIDTH APP_WIDTH) 2) (/ (- SCREEN_HEIGHT APP_HEIGHT) 2) 
        APP_WIDTH APP_HEIGHT)
      (.setResizable false)
      (.setIconImage cat-image)
      (.setContentPane (get-content-pane))
      (.addWindowListener (get-window-listener j-frame)))))

(defn get-about-menu-listener []
  (proxy [ActionListener] []
    (actionPerformed [evt]
      (JOptionPane/showMessageDialog nil "Efe Ariaroo made this. \nYou can change the text here."))))

(defn get-window-listener [j-frame]
  (proxy [WindowAdapter] []
    (windowClosing [window-event]
      (let [answer (JOptionPane/showConfirmDialog 
                     j-frame "Running away so soon?" 
                     (str "Prevent " (him-or-her) " from leaving") 0)]
        (if (zero? answer)
          (do 
            (JOptionPane/showMessageDialog j-frame "Fine, runaway"))
          (do 
            (JOptionPane/showMessageDialog j-frame "You wanted to exit. No take backs")))))))

;;; the content pane things

(declare init-content-pane get-done-button-listener 
         get-retry-button-listener draw-models 
         all-asses-selected gay-straight-for-male)
(def combo-boxes (make-array JComboBox 3))

(defn get-content-pane []
  (let [bottom-panel (JPanel.)
        bottom-panel-top (JPanel.)
        bottom-panel-bottom (JPanel. (FlowLayout. 1 10 5))        
        done-button (JButton. "Done")
        retry-button (JButton. "Retry")
        the-pane (init-content-pane)]
    (dotimes [i 3]
      (aset combo-boxes i (doto (JComboBox.)
                            (.setPreferredSize (Dimension. 60 47))
                            (.addItem "----")
                            (.addItem "Like")
                            (.addItem "Don't Like")
                            (.setBorder (TitledBorder. (str "Ass " (inc i)))))))
    (.addActionListener done-button (get-done-button-listener the-pane))
    (.addActionListener retry-button (get-retry-button-listener the-pane))

    (doto bottom-panel-top
      (.add (aget combo-boxes 0))
      (.add (aget combo-boxes 1))
      (.add (aget combo-boxes 2)))
    (doto bottom-panel-bottom
      (.add done-button)
      (.add retry-button))
    (doto bottom-panel
      (.setLayout (BorderLayout.))
      (.add bottom-panel-top "North")
      (.add bottom-panel-bottom "South"))
    (doto the-pane
      (.setLayout (BorderLayout.))
      (.setBackground (Color. (.getRGB (first models-images) 2 2)))
      (.add bottom-panel "South"))))

(defn init-content-pane []
  (proxy [JPanel] []
    (paintComponent [^Graphics graphics]
      (proxy-super paintComponent graphics)
      (if @reveal-full-images
        (draw-models graphics)
        (do 
          (draw-models graphics)
          (doto graphics
            (.setColor (Color. (.getRGB (first models-images) 2 5)))
            (.fillRect 0 0 APP_WIDTH (- (/ APP_HEIGHT 2) 80))
            (.fillRect 0 (- (/ APP_HEIGHT 2) 5) APP_WIDTH 
              (- APP_HEIGHT (/ APP_HEIGHT 2) 5))))))))

(defn draw-models [graphics]
  (doto graphics
    (.setColor (Color. (.getRGB (first models-images) 2 5)))
    (.fillRect 0 0 APP_WIDTH APP_HEIGHT)
    (.drawImage (nth models-images (@rand-image-indices 0)) 0 0 nil)
    (.drawImage (nth models-images (@rand-image-indices 1))
      (.getWidth (nth models-images (@rand-image-indices 0))) 0 nil)
    (.drawImage (nth models-images (@rand-image-indices 2))
      (+ (.getWidth (nth models-images (@rand-image-indices 0)))
         (.getWidth (nth models-images (@rand-image-indices 1)))) 0 nil)))

;;; This is the action listener for the 'Done' button
;;; We need this if we want the 'Done' button to do
;;; anything once it is clicked
(defn get-done-button-listener [content-pane]
  (proxy [ActionListener] []
    (actionPerformed [evt]
      (try 
        ;;; This commented out code always causes an error if 
        ;;; no asses are selected. If anyone has any suggestions 
        ;;; for this please let me know. Gracias Mucho.
        #_(dotimes [i 3]
           (if (zero? (.getSelectedIndex (aget combo-boxes i)))
             (do 
               (JOptionPane/showMessageDialog nil? (str "You didn't pick any for ass: " 
                                                    (inc i) " silly"))
               (throw (NullPointerException. 
                         "the user didn't pick anything for an ass")))))
        (reset! gay-count 0)
        (reset! straight-count 0)

        (all-asses-selected content-pane)
        (reset! reveal-full-images true)
        (.repaint content-pane)
        (catch NullPointerException e (str "caught exception: " 
                                           (.getMessage e)))))))

(defn all-asses-selected [^JPanel content-pane]
  "This function will be called once all the asses are 
   selected"
  (dotimes [i 3]
    (aset answers i (if (= (.getSelectedIndex (aget combo-boxes i)) 1)
                      true false))) 
  (if @gender
    (gay-straight-for-male answers)
    (do (gay-straight-for-male answers)
      (let [gay @gay-count
            straight @straight-count]
        (reset! gay-count straight)
        (reset! straight-count gay))))
      (cond 
        (= @gay-count @straight-count)
        (JOptionPane/showMessageDialog nil "You are a little gay")
        (> @gay-count @straight-count)
        (JOptionPane/showMessageDialog nil "You are so gay")
        (> @straight-count @gay-count)
        (JOptionPane/showMessageDialog nil "You are straight")) )


;;; I don't want to duplicate logic so I perform the logic
;;; for when the user is male i.e gender is true.
;;; If the user is female I simply have to reverse the
;;; values of gay-count and straight-count.

;;; I know the way things are done in this function aren't
;;; "" "" functional but we shouldn't throw the baby out with
;;; the bath water. Sometimes we gotta change 'em some state.
;;; Since nothing outside this namespace is involved
;;; and since this namespace is small enough some state change is ok.
(defn gay-straight-for-male [answers]
  (dotimes [i 3]
    (case (nth @rand-image-indices i)
      0 (if (aget answers i)
          (reset! gay-count (inc @gay-count))
          (reset! straight-count (inc @straight-count)))
      1 (if (aget answers i)
          (reset! straight-count (inc @straight-count))
          (reset! gay-count (inc @gay-count)))
      2 (if (aget answers i)
          (reset! gay-count (inc @gay-count))
          (reset! straight-count (inc @straight-count)))
      3 (if (aget answers i)
          (reset! straight-count (inc @straight-count))
          (reset! gay-count (inc @gay-count)))
      4 (if (aget answers i)
          (reset! gay-count (inc @gay-count))
          (reset! straight-count (inc @straight-count)))
      5 (if (aget answers i)
          (reset! straight-count (inc @straight-count))
          (reset! gay-count (inc @gay-count)))
      6 (if (aget answers i)
          (reset! straight-count (inc @straight-count))
          (reset! gay-count (inc @gay-count))))))

(defn get-retry-button-listener [^JPanel content-pane]
  (proxy [ActionListener] []
    (actionPerformed [evt]
      (reset! reveal-full-images false)
      (reset! rand-image-indices [(rand-int 7) 
                                  (rand-int 7) (rand-int 7)])
      (.repaint content-pane))))


;;; for the dialog that pronounces the user
;;; gay or straight
(declare init-message-dialog) 

(defn get-message-dialog [^BufferedImage first-image ^JFrame j-frame 
                          ^String string-1 ^String string-2]
  (let [j-frame-size (.getSize j-frame) 
        j-frame-location (.getLocation j-frame) 
        center-panel (JPanel.)
        south-panel (JPanel.)
        ok-button (JButton. "OK")
        the-dialog (init-message-dialog j-frame string-1)]
    (.addActionListener ok-button the-dialog)
    (doto center-panel
      (.setBackground (Color. (.getRGB first-image 2 2)))
      (.setLayout (BorderLayout.))
      (.add (JLabel. string-2) "Center"))
    (doto south-panel
      (.setBackground (Color. (.getRGB first-image 2 2)))
      (.add (ok-button)))
    (doto the-dialog 
      (.add (.getContentPane the-dialog) center-panel "Center")
      (.add (.getContentPane the-dialog) south-panel "South")
      (.setLocation 
        (+ (.x j-frame-location) (.width j-frame-size))
        (+ (.y j-frame-location) (.height j-frame-size)))
      (.setDefaultCloseOperation 2)
      (.pack)
      (.setVisible true))))

(defn init-message-dialog [j-frame string-1]
  (proxy [JDialog ActionListener] 
    [j-frame string-1 true]
    (actionPerformed [evt]
      (.setVisible this false)
      (.dispose this))))


;;; For the initial splash view
(declare init-splash-holder)

(defn get-splash-holder []
  (let [the-splash (init-splash-holder)]
    (doto the-splash
      (.setBackground (Color/black)))))

(defn init-splash-holder []
  (let [splash-text-font (Font. "SanSerif" 350 20)]
    (proxy [JPanel] []
      (paintComponent [graphics]
        (proxy-super paintComponent graphics)
        (doto graphics
          (.setColor (Color/red))
          (.setFont splash-text-font)
          (.drawString "GODS" 350 20)
          (.drawString "Many strange things," 20 50)
          #_(.drawString "One thing is certain . . . " 20 80)
          (.drawString "Gay or Straight" 20 110))))))


;;; Where all the magic begins
(defn -main []
  (let [splash-screen (JWindow.)
        j 400 k 130
        window-title (set-gender-get-title)
        the-jframe (get-j-frame)]
    (doto splash-screen
      (.setContentPane (get-splash-holder))
      (.setBounds (/ (- SCREEN_WIDTH j) 2) (/ (- SCREEN_HEIGHT k) 2) j k)
      (.setVisible true))
    (Thread/sleep 7000)
    (.dispose splash-screen)
    
    (doto the-jframe
     (.setTitle window-title)
     (.setVisible true))))

