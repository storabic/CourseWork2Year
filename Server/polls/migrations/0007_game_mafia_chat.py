# Generated by Django 3.0.5 on 2020-05-10 22:26

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('polls', '0006_game_chat'),
    ]

    operations = [
        migrations.AddField(
            model_name='game',
            name='mafia_chat',
            field=models.TextField(default=''),
        ),
    ]