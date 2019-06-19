from PIL import Image, ImageDraw, ImageFont
import mysql.connector


def draw_and_save(im_, box_list_, font_):
    # TODO: automatically align label
    for box in box_list_:
        label = box['label']
        label_w, label_h = font.getsize(label)

        xmax = box['xmax'] * width
        xmin = box['xmin'] * width
        ymax = box['ymax'] * height
        ymin = box['ymin'] * height
        label_xmax = xmin + label_w
        label_ymax = ymax + label_h

        draw.rectangle(((xmin, ymin), (xmax, ymax)), outline='red', width=2)
        draw.rectangle(((xmin, ymax), (label_xmax, label_ymax)), fill='red')
        draw.text((xmin, ymax), label, font=font_)
    
    im.show()


db = mysql.connector.connect(
    host="localhost",
    user="springuser",
    passwd="ThePassword",
    db="tf_detection"
    )
cur = db.cursor(dictionary=True)

font = ImageFont.truetype("/Library/Fonts/Arial.ttf", 20)

cur.execute("SELECT t.* FROM tf_detection.inference_files t LIMIT 501")
file_list = cur.fetchall()

for row in file_list:
    print(f"id: {row['id']}")

    im = Image.open(row['file_name'])
    width , height = im.size
    draw = ImageDraw.Draw(im)

    box_list = cur.execute(f"SELECT label, xmax, xmin, ymax, ymin FROM tf_detection.results WHERE file_index ={row['id']} LIMIT 501")
    box_list = cur.fetchall()
    print(box_list)

    draw_and_save(im, box_list, font)
