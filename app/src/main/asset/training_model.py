import numpy as np
import tensorflow as tf

# -----------------------------
# Generate synthetic training data
# -----------------------------
# Features: [accel, gyro, steps, time]
# Target: speed (m/s)

X = []
y = []

for i in range(1000):
    accel = np.random.uniform(9, 15)        # realistic accel magnitude
    gyro = np.random.uniform(0, 5)
    steps = np.random.uniform(1, 100)
    time = np.random.uniform(1, 60)

    # simple "ground truth" formula (simulate real walking)
    speed = (steps / time) * 0.75 + (accel - 9) * 0.05

    X.append([accel, gyro, steps, time])
    y.append(speed)

X = np.array(X)
y = np.array(y)

# -----------------------------
# Build model
# -----------------------------
model = tf.keras.Sequential([
    tf.keras.layers.Dense(16, activation='relu', input_shape=(4,)),
    tf.keras.layers.Dense(8, activation='relu'),
    tf.keras.layers.Dense(1)
])

model.compile(optimizer='adam', loss='mse')

# -----------------------------
# Train
# -----------------------------
model.fit(X, y, epochs=20)

# -----------------------------
# Convert to TFLite
# -----------------------------
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

# Save model
with open("walking_model.tflite", "wb") as f:
    f.write(tflite_model)

print("Model saved as walking_model.tflite")
